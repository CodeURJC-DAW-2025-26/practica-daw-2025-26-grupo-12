package es.codeurjc.grupo12.scissors_please.service;

import es.codeurjc.grupo12.scissors_please.model.Bot;
import es.codeurjc.grupo12.scissors_please.model.Match;
import es.codeurjc.grupo12.scissors_please.model.Round;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.repository.BotRepository;
import es.codeurjc.grupo12.scissors_please.repository.MatchRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class MatchService {

  private static final int MAX_PAGE_SIZE = 20;
  private static final int BASE_ELO_GAP = 120;
  private static final int ELO_GAP_STEP = 40;
  private static final int ELO_GAP_STEP_SECONDS = 5;
  private static final Duration SEARCH_EXPIRATION = Duration.ofMinutes(5);
  private static final Duration READY_MATCH_EXPIRATION = Duration.ofMinutes(5);
  private static final Duration REMATCH_INVITATION_EXPIRATION = Duration.ofMinutes(2);
  private static final Duration MIN_READY_REDIRECT_DELAY = Duration.ofSeconds(1);
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
  private static final List<String> MOVES = List.of("Rock", "Paper", "Scissors");

  @Autowired private MatchRepository matchRepository;
  @Autowired private BotRepository botRepository;
  @Autowired private NotificationService notificationService;
  @Autowired private UserService userService;

  private final Object matchmakingMonitor = new Object();
  private final Map<Long, SearchTicket> searchQueue = new ConcurrentHashMap<>();
  private final Map<Long, ReadyMatch> readyMatches = new ConcurrentHashMap<>();
  private final Map<String, RematchInvitation> rematchInvitations = new ConcurrentHashMap<>();
  private final Map<Long, RematchInvitation> pendingRematchesByRequester = new ConcurrentHashMap<>();

  public MatchStartResult startMatchmaking(Long userId, String username, Long selectedBotId) {
    Bot myBot = resolveMyBot(userId, selectedBotId);
    LocalDateTime now = LocalDateTime.now();

    synchronized (matchmakingMonitor) {
      purgeExpiredState(now);

      ReadyMatch readyMatch = getValidReadyMatch(userId);
      if (readyMatch != null) {
        return MatchStartResult.matched(
            readyMatch.matchId(), readyMatch.myBotName(), readyMatch.opponentBotName());
      }

      SearchTicket ticket = new SearchTicket(userId, username, myBot, now);
      searchQueue.put(userId, ticket);

      ReadyMatch createdMatch = tryMatch(ticket, now);
      if (createdMatch != null) {
        return MatchStartResult.matched(
            createdMatch.matchId(), createdMatch.myBotName(), createdMatch.opponentBotName());
      }

      return MatchStartResult.searching(resolveBotName(myBot));
    }
  }

  public String requestRematch(Long matchId, User requester) {
    if (requester == null || requester.getId() == null) {
      throw new IllegalArgumentException("User not authenticated");
    }

    LocalDateTime now = LocalDateTime.now();
    synchronized (matchmakingMonitor) {
      purgeExpiredState(now);

      Match previousMatch = resolveMatch(matchId);
      RematchParticipants participants = resolveRematchParticipants(previousMatch, requester.getId());

      clearPendingRematchForRequester(requester.getId());

      String invitationId = UUID.randomUUID().toString();
      RematchInvitation invitation =
          new RematchInvitation(
              invitationId,
              previousMatch.getId(),
              requester.getId(),
              requester.getUsername(),
              participants.requesterBot(),
              participants.opponentUser().getId(),
              participants.opponentUser().getUsername(),
              participants.opponentBot(),
              previousMatch.getBot1(),
              previousMatch.getBot2(),
              now);

      rematchInvitations.put(invitationId, invitation);
      pendingRematchesByRequester.put(requester.getId(), invitation);

      notificationService.sendNotification(
          participants.opponentUser().getUsername(),
          NotificationService.NotificationPayload.action(
              "rematch_request",
              requester.getUsername()
                  + " wants a rematch with the same bots: "
                  + resolveBotName(participants.requesterBot())
                  + " vs "
                  + resolveBotName(participants.opponentBot()),
              "Accept",
              "/matches/rematch/accept?id=" + invitationId,
              null));

      return invitationId;
    }
  }

  public String acceptRematch(String invitationId, User acceptingUser) {
    if (acceptingUser == null || acceptingUser.getId() == null) {
      throw new IllegalArgumentException("User not authenticated");
    }

    LocalDateTime now = LocalDateTime.now();
    synchronized (matchmakingMonitor) {
      purgeExpiredState(now);

      RematchInvitation invitation = rematchInvitations.get(invitationId);
      if (invitation == null) {
        throw new IllegalArgumentException("Rematch invitation is no longer available.");
      }
      if (!acceptingUser.getId().equals(invitation.opponentUserId())) {
        throw new IllegalArgumentException("You cannot accept this rematch invitation.");
      }

      Match rematch = createRematch(invitation, now);
      clearPendingRematch(invitation);

      ReadyMatch requesterReady = buildReadyMatchForRematch(invitation, rematch, true, now);
      ReadyMatch opponentReady = buildReadyMatchForRematch(invitation, rematch, false, now);
      readyMatches.put(invitation.requesterUserId(), requesterReady);
      readyMatches.put(invitation.opponentUserId(), opponentReady);

      String redirectUrl = requesterReady.redirectUrl();
      notificationService.sendNotification(
          invitation.requesterUsername(),
          NotificationService.NotificationPayload.redirect(
              "rematch_ready",
              acceptingUser.getUsername() + " accepted your rematch.",
              redirectUrl));
      notificationService.sendNotification(
          acceptingUser.getUsername(),
          NotificationService.NotificationPayload.info(
              "rematch_ready",
              "Rematch accepted. Opening results...",
              redirectUrl));

      return redirectUrl;
    }
  }

  public boolean canRequestRematch(Long matchId, Long userId) {
    if (matchId == null || userId == null) {
      return false;
    }

    try {
      Match match = resolveMatch(matchId);
      return canOwnExactlyOneSide(match, userId);
    } catch (IllegalArgumentException exception) {
      return false;
    }
  }

  public MatchmakingStatusView getMatchmakingStatus(Long userId) {
    LocalDateTime now = LocalDateTime.now();

    synchronized (matchmakingMonitor) {
      purgeExpiredState(now);

      ReadyMatch readyMatch = getValidReadyMatch(userId);
      if (readyMatch != null) {
        if (!isReadyForRedirect(readyMatch, now)) {
          return MatchmakingStatusView.searching(
              readyMatch.myBotId(),
              readyMatch.myBotName(),
              readyMatch.myBotElo(),
              readyMatch.myBotDescription(),
              0,
              0);
        }
        return MatchmakingStatusView.matched(
            readyMatch.matchId(),
            readyMatch.redirectUrl(),
            readyMatch.myBotId(),
            readyMatch.myBotName(),
            readyMatch.myBotElo(),
            readyMatch.myBotDescription(),
            readyMatch.opponentBotName());
      }

      RematchInvitation pendingRematch = pendingRematchesByRequester.get(userId);
      if (pendingRematch != null) {
        long waitSeconds = Math.max(0, Duration.between(pendingRematch.createdAt(), now).getSeconds());
        return MatchmakingStatusView.searching(
            pendingRematch.requesterBot().getId(),
            resolveBotName(pendingRematch.requesterBot()),
            resolveBotElo(pendingRematch.requesterBot()),
            resolveBotDescription(pendingRematch.requesterBot()),
            waitSeconds,
            1);
      }

      SearchTicket ticket = searchQueue.get(userId);
      if (ticket == null) {
        return MatchmakingStatusView.idle();
      }

      long waitSeconds = Math.max(0, Duration.between(ticket.createdAt(), now).getSeconds());
      int playersSearching = searchQueue.size();
      return MatchmakingStatusView.searching(
          ticket.bot().getId(),
          resolveBotName(ticket.bot()),
          resolveBotElo(ticket.bot()),
          resolveBotDescription(ticket.bot()),
          waitSeconds,
          playersSearching);
    }
  }

  public boolean hasActiveMatchmaking(Long userId) {
    synchronized (matchmakingMonitor) {
      purgeExpiredState(LocalDateTime.now());
      ReadyMatch readyMatch = getValidReadyMatch(userId);
      return searchQueue.containsKey(userId)
          || readyMatch != null
          || pendingRematchesByRequester.containsKey(userId);
    }
  }

  public void cancelMatchmaking(Long userId) {
    synchronized (matchmakingMonitor) {
      ReadyMatch readyMatch = getValidReadyMatch(userId);
      if (readyMatch != null) {
        throw new IllegalArgumentException("Match already found. Opening battle...");
      }
      RematchInvitation pendingRematch = pendingRematchesByRequester.remove(userId);
      if (pendingRematch != null) {
        rematchInvitations.remove(pendingRematch.id());
      }
      searchQueue.remove(userId);
    }
  }

  public void acknowledgeReadyMatch(Long userId, Long matchId) {
    if (matchId == null) {
      return;
    }

    synchronized (matchmakingMonitor) {
      ReadyMatch readyMatch = readyMatches.get(userId);
      if (readyMatch != null && matchId.equals(readyMatch.matchId())) {
        readyMatches.remove(userId);
      }
    }
  }

  public MatchBattleView getMatchBattleView(Long matchId) {
    Match match = resolveMatch(matchId);
    String bot1Name = resolveBotName(match.getBot1());
    String bot2Name = resolveBotName(match.getBot2());
    return new MatchBattleView(
        match.getId(),
        bot1Name,
        resolveInitial(bot1Name),
        bot2Name,
        resolveInitial(bot2Name),
        "/matches/stats?id=" + match.getId());
  }

  public MatchStatsView getMatchStatsView(Long matchId) {
    Match match = resolveMatch(matchId);
    String bot1Name = resolveBotName(match.getBot1());
    String bot2Name = resolveBotName(match.getBot2());
    String winnerLabel = resolveWinnerLabel(match, bot1Name, bot2Name);
    String winnerBadgeClass =
        "Draw".equalsIgnoreCase(winnerLabel) ? "bg-secondary" : "badge-soft-success";

    List<MatchRoundView> rounds =
        Optional.ofNullable(match.getRounds()).orElse(List.of()).stream()
            .sorted(Comparator.comparingInt(Round::getRoundNumber))
            .map(this::toRoundView)
            .toList();

    return new MatchStatsView(
        match.getId(),
        bot1Name,
        bot2Name,
        winnerLabel,
        winnerBadgeClass,
        match.getBot1Score(),
        match.getBot2Score(),
        rounds.size(),
        formatDate(match.getTimestamp()),
        rounds);
  }

  public MatchPage getBestMatchPage(Pageable pageable) {
    int safePage = Math.max(pageable.getPageNumber(), 0);
    int safeSize = Math.min(Math.max(pageable.getPageSize(), 1), MAX_PAGE_SIZE);
    Pageable safePageable = PageRequest.of(safePage, safeSize);

    Page<Match> pageResult = matchRepository.findBestMatches(safePageable);
    List<MatchListItem> matches = pageResult.getContent().stream().map(this::toListItem).toList();
    long totalElements = pageResult.getTotalElements();
    int fromItem = matches.isEmpty() ? 0 : (safePage * safeSize) + 1;
    int toItem = matches.isEmpty() ? 0 : fromItem + matches.size() - 1;

    return new MatchPage(
        matches, safePage + 1, pageResult.hasNext(), totalElements, fromItem, toItem);
  }

  public List<UserMatchItem> getUserHomeMatches(Long userId, int limit) {
    int safeLimit = Math.max(limit, 0);
    if (safeLimit == 0) {
      return List.of();
    }

    return matchRepository
        .findDistinctByBot1OwnerIdOrBot2OwnerIdOrderByTimestampDesc(userId, userId)
        .stream()
        .limit(safeLimit)
        .map(match -> toUserMatchItem(match, userId, true))
        .toList();
  }

  public UserRecentMatchSection getUserRecentMatchSection(
      Long userId, String participationFilterParam) {
    ParticipationFilter participationFilter =
        ParticipationFilter.fromParam(participationFilterParam);

    List<Match> allMatches = matchRepository.findAllByOrderByTimestampDesc();
    Set<Long> playedMatchIds =
        matchRepository
            .findDistinctByBot1OwnerIdOrBot2OwnerIdOrderByTimestampDesc(userId, userId)
            .stream()
            .map(Match::getId)
            .collect(Collectors.toSet());

    List<UserMatchItem> matches =
        allMatches.stream()
            .filter(
                match ->
                    matchesParticipationFilter(match.getId(), playedMatchIds, participationFilter))
            .map(match -> toUserMatchItem(match, userId, playedMatchIds.contains(match.getId())))
            .toList();

    return new UserRecentMatchSection(
        matches,
        participationFilter == ParticipationFilter.ALL,
        participationFilter == ParticipationFilter.PLAYED,
        participationFilter == ParticipationFilter.NOT_PLAYED);
  }

  @Scheduled(fixedDelay = 30000)
  void cleanupMatchmakingState() {
    synchronized (matchmakingMonitor) {
      purgeExpiredState(LocalDateTime.now());
    }
  }

  private ReadyMatch tryMatch(SearchTicket requester, LocalDateTime now) {
    SearchTicket currentRequester = searchQueue.get(requester.userId());
    if (currentRequester == null) {
      return null;
    }

    Optional<SearchTicket> candidate =
        searchQueue.values().stream()
            .filter(ticket -> !ticket.userId().equals(currentRequester.userId()))
            .filter(ticket -> isCandidateCompatible(currentRequester, ticket, now))
            .min(Comparator.comparingLong(ticket -> calculateCandidateScore(currentRequester, ticket, now)));

    if (candidate.isEmpty()) {
      return null;
    }

    SearchTicket opponent = candidate.get();
    Match match = createMatch(currentRequester, opponent, now);

    searchQueue.remove(currentRequester.userId());
    searchQueue.remove(opponent.userId());

    ReadyMatch requesterReady = buildReadyMatch(currentRequester, opponent, match, now);
    ReadyMatch opponentReady = buildReadyMatch(opponent, currentRequester, match, now);
    readyMatches.put(currentRequester.userId(), requesterReady);
    readyMatches.put(opponent.userId(), opponentReady);

    sendMatchFoundNotification(currentRequester, opponent, match.getId());
    return requesterReady;
  }

  private Match createMatch(SearchTicket requester, SearchTicket opponent, LocalDateTime now) {
    SearchTicket first =
        requester.createdAt().isAfter(opponent.createdAt()) ? opponent : requester;
    SearchTicket second = first == requester ? opponent : requester;

    Match match = new Match();
    match.setBot1(first.bot());
    match.setBot2(second.bot());
    match.setTimestamp(now);
    populateRandomResult(match);
    return matchRepository.save(match);
  }

  private ReadyMatch buildReadyMatch(
      SearchTicket ticket, SearchTicket opponent, Match match, LocalDateTime now) {
    return new ReadyMatch(
        ticket.userId(),
        match.getId(),
        "/matches/battle?id=" + match.getId(),
        ticket.bot().getId(),
        resolveBotName(ticket.bot()),
        resolveBotElo(ticket.bot()),
        resolveBotDescription(ticket.bot()),
        resolveBotName(opponent.bot()),
        now);
  }

  private void sendMatchFoundNotification(SearchTicket requester, SearchTicket opponent, Long matchId) {
    notificationService.createAndSendNotification(
        List.of(requester.username(), opponent.username()),
        NotificationService.NotificationPayload.info(
            "match_found",
            "Match found: "
                + resolveBotName(requester.bot())
                + " vs "
                + resolveBotName(opponent.bot())
                + " (#"
                + matchId
                + ")",
            null));
  }

  private Match createRematch(RematchInvitation invitation, LocalDateTime now) {
    Match match = new Match();
    match.setBot1(invitation.originalBot1());
    match.setBot2(invitation.originalBot2());
    match.setTimestamp(now);
    populateRandomResult(match);
    return matchRepository.save(match);
  }

  private ReadyMatch buildReadyMatchForRematch(
      RematchInvitation invitation, Match match, boolean requesterSide, LocalDateTime now) {
    Bot myBot = requesterSide ? invitation.requesterBot() : invitation.opponentBot();
    Bot opponentBot = requesterSide ? invitation.opponentBot() : invitation.requesterBot();
    Long userId = requesterSide ? invitation.requesterUserId() : invitation.opponentUserId();
    return new ReadyMatch(
        userId,
        match.getId(),
        "/matches/battle?id=" + match.getId(),
        myBot.getId(),
        resolveBotName(myBot),
        resolveBotElo(myBot),
        resolveBotDescription(myBot),
        resolveBotName(opponentBot),
        now);
  }

  private RematchParticipants resolveRematchParticipants(Match match, Long requesterUserId) {
    boolean requesterOwnsBot1 = isOwnedByUser(match.getBot1(), requesterUserId);
    boolean requesterOwnsBot2 = isOwnedByUser(match.getBot2(), requesterUserId);
    if (requesterOwnsBot1 == requesterOwnsBot2) {
      throw new IllegalArgumentException("You can only request a rematch from one of your own matches.");
    }

    Bot requesterBot = requesterOwnsBot1 ? match.getBot1() : match.getBot2();
    Bot opponentBot = requesterOwnsBot1 ? match.getBot2() : match.getBot1();
    if (opponentBot == null || opponentBot.getOwnerId() == null) {
      throw new IllegalArgumentException("The previous opponent is no longer available.");
    }

    User opponentUser = userService.getUserById(opponentBot.getOwnerId());
    return new RematchParticipants(requesterBot, opponentBot, opponentUser);
  }

  private boolean canOwnExactlyOneSide(Match match, Long userId) {
    return isOwnedByUser(match.getBot1(), userId) ^ isOwnedByUser(match.getBot2(), userId);
  }

  private boolean isCandidateCompatible(
      SearchTicket requester, SearchTicket candidate, LocalDateTime now) {
    if (requester.bot().getId() != null && requester.bot().getId().equals(candidate.bot().getId())) {
      return false;
    }

    int eloDifference =
        Math.abs(resolveBotElo(requester.bot()) - resolveBotElo(candidate.bot()));
    return eloDifference <= acceptableEloGap(requester, candidate, now);
  }

  private long calculateCandidateScore(
      SearchTicket requester, SearchTicket candidate, LocalDateTime now) {
    long requesterWait = Math.max(0, Duration.between(requester.createdAt(), now).getSeconds());
    long candidateWait = Math.max(0, Duration.between(candidate.createdAt(), now).getSeconds());
    int eloDifference =
        Math.abs(resolveBotElo(requester.bot()) - resolveBotElo(candidate.bot()));

    long waitBonus = Math.min(requesterWait + candidateWait, 180);
    long fairnessPenalty = Math.abs(requesterWait - candidateWait);
    return (long) eloDifference * 10L + fairnessPenalty - (waitBonus * 2L);
  }

  private int acceptableEloGap(SearchTicket requester, SearchTicket candidate, LocalDateTime now) {
    long requesterWait = Math.max(0, Duration.between(requester.createdAt(), now).getSeconds());
    long candidateWait = Math.max(0, Duration.between(candidate.createdAt(), now).getSeconds());
    int requesterGap = dynamicEloGap(requesterWait);
    int candidateGap = dynamicEloGap(candidateWait);
    return Math.max(requesterGap, candidateGap);
  }

  private int dynamicEloGap(long waitSeconds) {
    long steps = waitSeconds / ELO_GAP_STEP_SECONDS;
    return BASE_ELO_GAP + (int) steps * ELO_GAP_STEP;
  }

  private void purgeExpiredState(LocalDateTime now) {
    searchQueue.entrySet().removeIf(entry -> isOlderThan(entry.getValue().createdAt(), SEARCH_EXPIRATION, now));
    readyMatches.entrySet().removeIf(entry -> isOlderThan(entry.getValue().createdAt(), READY_MATCH_EXPIRATION, now));
    rematchInvitations.values().stream()
        .filter(invitation -> isOlderThan(invitation.createdAt(), REMATCH_INVITATION_EXPIRATION, now))
        .toList()
        .forEach(this::clearPendingRematch);
  }

  private boolean isOlderThan(LocalDateTime timestamp, Duration ttl, LocalDateTime now) {
    return timestamp == null || Duration.between(timestamp, now).compareTo(ttl) > 0;
  }

  private boolean isReadyForRedirect(ReadyMatch readyMatch, LocalDateTime now) {
    return readyMatch.createdAt() != null
        && !Duration.between(readyMatch.createdAt(), now).minus(MIN_READY_REDIRECT_DELAY).isNegative();
  }

  private ReadyMatch getValidReadyMatch(Long userId) {
    ReadyMatch readyMatch = readyMatches.get(userId);
    if (readyMatch == null) {
      return null;
    }

    if (readyMatch.matchId() == null || !matchRepository.existsById(readyMatch.matchId())) {
      readyMatches.remove(userId);
      return null;
    }

    return readyMatch;
  }

  private void clearPendingRematchForRequester(Long requesterUserId) {
    RematchInvitation invitation = pendingRematchesByRequester.remove(requesterUserId);
    if (invitation != null) {
      rematchInvitations.remove(invitation.id());
    }
  }

  private void clearPendingRematch(RematchInvitation invitation) {
    rematchInvitations.remove(invitation.id());
    pendingRematchesByRequester.remove(invitation.requesterUserId());
  }

  private boolean matchesParticipationFilter(
      Long matchId, Set<Long> playedMatchIds, ParticipationFilter participationFilter) {
    boolean played = playedMatchIds.contains(matchId);
    if (participationFilter == ParticipationFilter.PLAYED) {
      return played;
    }
    if (participationFilter == ParticipationFilter.NOT_PLAYED) {
      return !played;
    }
    return true;
  }

  private UserMatchItem toUserMatchItem(Match match, Long userId, boolean played) {
    Bot bot1 = match.getBot1();
    Bot bot2 = match.getBot2();

    String myBotName = "-";
    String opponentName = resolveBotName(bot1) + " vs " + resolveBotName(bot2);
    String result = resolveResult(match);

    if (played) {
      boolean bot1Owned = isOwnedByUser(bot1, userId);
      boolean bot2Owned = isOwnedByUser(bot2, userId);
      if (bot1Owned && !bot2Owned) {
        myBotName = resolveBotName(bot1);
        opponentName = resolveBotName(bot2);
        result = resolveResultFromScores(match.getBot1Score(), match.getBot2Score());
      } else if (!bot1Owned && bot2Owned) {
        myBotName = resolveBotName(bot2);
        opponentName = resolveBotName(bot1);
        result = resolveResultFromScores(match.getBot2Score(), match.getBot1Score());
      } else if (bot1Owned) {
        myBotName = resolveBotName(bot1);
        opponentName = resolveBotName(bot2);
        result = resolveResult(match);
      }
    }

    String participationLabel = played ? "Played" : "Not Played";
    String participationBadgeClass =
        played ? "bg-secondary" : "bg-dark border border-secondary text-secondary";

    return new UserMatchItem(
        match.getId(),
        myBotName,
        opponentName,
        result,
        resolveBadgeClass(result),
        formatDate(match.getTimestamp()),
        played,
        participationLabel,
        participationBadgeClass,
        "/matches/stats?id=" + match.getId());
  }

  private boolean isOwnedByUser(Bot bot, Long userId) {
    return bot != null && bot.getOwnerId() != null && bot.getOwnerId().equals(userId);
  }

  private Bot resolveMyBot(Long userId, Long selectedBotId) {
    List<Bot> userBots = botRepository.findByOwnerId(userId);
    if (userBots.isEmpty()) {
      throw new IllegalArgumentException("You need at least one bot to start matchmaking.");
    }

    if (selectedBotId != null) {
      return userBots.stream()
          .filter(bot -> selectedBotId.equals(bot.getId()))
          .findFirst()
          .orElseThrow(() -> new IllegalArgumentException("Selected bot is not available."));
    }

    return userBots.stream().max(Comparator.comparingInt(Bot::getElo)).orElseThrow();
  }

  private void populateRandomResult(Match match) {
    boolean bot1Wins = ThreadLocalRandom.current().nextBoolean();
    int losingScore = ThreadLocalRandom.current().nextInt(6, 10);
    int winningScore = losingScore + ThreadLocalRandom.current().nextInt(1, 4);

    int bot1Score = bot1Wins ? winningScore : losingScore;
    int bot2Score = bot1Wins ? losingScore : winningScore;
    match.setBot1Score(bot1Score);
    match.setBot2Score(bot2Score);

    String winnerName =
        bot1Wins ? resolveBotName(match.getBot1()) : resolveBotName(match.getBot2());
    match.setResult(winnerName + " Wins");
    match.setRounds(generateRounds(bot1Score, bot2Score));
  }

  private List<Round> generateRounds(int bot1Wins, int bot2Wins) {
    List<Round> rounds = new ArrayList<>();
    for (int i = 0; i < bot1Wins; i++) {
      rounds.add(createRound(true));
    }
    for (int i = 0; i < bot2Wins; i++) {
      rounds.add(createRound(false));
    }
    Collections.shuffle(rounds);
    for (int i = 0; i < rounds.size(); i++) {
      rounds.get(i).setRoundNumber(i + 1);
    }
    return rounds;
  }

  private Round createRound(boolean bot1Win) {
    String baseMove = randomMove();
    Round round = new Round();
    if (bot1Win) {
      round.setBot1Move(baseMove);
      round.setBot2Move(losingMoveAgainst(baseMove));
      round.setResult("Win");
    } else {
      round.setBot2Move(baseMove);
      round.setBot1Move(losingMoveAgainst(baseMove));
      round.setResult("Loss");
    }
    return round;
  }

  private String randomMove() {
    int index = ThreadLocalRandom.current().nextInt(MOVES.size());
    return MOVES.get(index);
  }

  private String losingMoveAgainst(String move) {
    return switch (move) {
      case "Rock" -> "Scissors";
      case "Paper" -> "Rock";
      default -> "Paper";
    };
  }

  private Match resolveMatch(Long matchId) {
    if (matchId != null) {
      return matchRepository
          .findById(matchId)
          .orElseThrow(() -> new IllegalArgumentException("Match not found."));
    }
    return matchRepository
        .findTopByOrderByTimestampDesc()
        .orElseThrow(() -> new IllegalArgumentException("No matches available yet."));
  }

  private String resolveWinnerLabel(Match match, String bot1Name, String bot2Name) {
    if (match.getBot1Score() > match.getBot2Score()) {
      return bot1Name + " Wins";
    }
    if (match.getBot2Score() > match.getBot1Score()) {
      return bot2Name + " Wins";
    }
    return "Draw";
  }

  private MatchRoundView toRoundView(Round round) {
    String result =
        round.getResult() == null || round.getResult().isBlank() ? "Draw" : round.getResult();
    return new MatchRoundView(
        round.getRoundNumber(),
        round.getBot1Move(),
        round.getBot2Move(),
        result,
        resolveBadgeClass(result));
  }

  private String resolveResultFromScores(int myScore, int opponentScore) {
    if (myScore == opponentScore) {
      return "Draw";
    }
    return myScore > opponentScore ? "Win" : "Loss";
  }

  private MatchListItem toListItem(Match match) {
    String bot1Name = resolveBotName(match.getBot1());
    String bot2Name = resolveBotName(match.getBot2());
    int topElo = Math.max(resolveBotElo(match.getBot1()), resolveBotElo(match.getBot2()));
    String result = resolveResult(match);
    String badgeClass = resolveBadgeClass(result);
    String date = formatDate(match.getTimestamp());

    return new MatchListItem(
        match.getId(),
        bot1Name,
        resolveInitial(bot1Name),
        bot2Name,
        resolveInitial(bot2Name),
        topElo,
        result,
        badgeClass,
        date,
        "/matches/stats?id=" + match.getId());
  }

  private String resolveBotName(Bot bot) {
    if (bot == null || bot.getName() == null || bot.getName().isBlank()) {
      return "Unknown";
    }
    return bot.getName();
  }

  private String resolveInitial(String name) {
    if (name == null || name.isBlank()) {
      return "?";
    }
    return name.substring(0, 1).toUpperCase();
  }

  private int resolveBotElo(Bot bot) {
    return bot == null ? 0 : bot.getElo();
  }

  private String resolveBotDescription(Bot bot) {
    if (bot == null || bot.getDescription() == null || bot.getDescription().isBlank()) {
      return "No description available for this bot yet.";
    }
    return bot.getDescription();
  }

  private String resolveResult(Match match) {
    if (match.getResult() != null && !match.getResult().isBlank()) {
      return match.getResult();
    }

    if (match.getBot1Score() == match.getBot2Score()) {
      return "Draw";
    }
    return match.getBot1Score() > match.getBot2Score() ? "Win" : "Loss";
  }

  private String resolveBadgeClass(String result) {
    String normalized = result == null ? "" : result.toLowerCase(Locale.ROOT);
    if (normalized.contains("draw") || normalized.contains("tie")) {
      return "bg-secondary";
    }
    if (normalized.contains("loss") || normalized.contains("lose")) {
      return "badge-soft-danger";
    }
    return "badge-soft-success";
  }

  private String formatDate(LocalDateTime timestamp) {
    if (timestamp == null) {
      return "Unknown";
    }

    Duration delta = Duration.between(timestamp, LocalDateTime.now());
    if (delta.isNegative()) {
      return DATE_FORMATTER.format(timestamp);
    }

    long minutes = delta.toMinutes();
    if (minutes < 1) {
      return "Just now";
    }
    if (minutes < 60) {
      return minutes == 1 ? "1 min ago" : minutes + " mins ago";
    }

    long hours = delta.toHours();
    if (hours < 24) {
      return hours == 1 ? "1 hour ago" : hours + " hours ago";
    }

    long days = delta.toDays();
    if (days < 7) {
      return days == 1 ? "1 day ago" : days + " days ago";
    }

    return DATE_FORMATTER.format(timestamp);
  }

  public record MatchListItem(
      Long id,
      String bot1Name,
      String bot1Initial,
      String bot2Name,
      String bot2Initial,
      int topElo,
      String result,
      String resultBadgeClass,
      String date,
      String actionHref) {}

  public record MatchPage(
      List<MatchListItem> matches,
      int nextPage,
      boolean hasMore,
      long totalElements,
      int fromItem,
      int toItem) {}

  public record UserMatchItem(
      Long id,
      String myBotName,
      String opponentName,
      String result,
      String resultBadgeClass,
      String date,
      boolean played,
      String participationLabel,
      String participationBadgeClass,
      String actionHref) {}

  public record UserRecentMatchSection(
      List<UserMatchItem> matches,
      boolean selectedAll,
      boolean selectedPlayed,
      boolean selectedNotPlayed) {}

  public record MatchStartResult(
      boolean matched, Long matchId, String myBotName, String opponentBotName, boolean searching) {
    static MatchStartResult matched(Long matchId, String myBotName, String opponentBotName) {
      return new MatchStartResult(true, matchId, myBotName, opponentBotName, false);
    }

    static MatchStartResult searching(String myBotName) {
      return new MatchStartResult(false, null, myBotName, null, true);
    }
  }

  public record MatchmakingStatusView(
      String state,
      boolean searching,
      boolean matched,
      Long matchId,
      String redirectUrl,
      Long selectedBotId,
      String selectedBotName,
      int selectedBotElo,
      String selectedBotDescription,
      String opponentBotName,
      long waitSeconds,
      int playersSearching) {
    static MatchmakingStatusView idle() {
      return new MatchmakingStatusView(
          "idle", false, false, null, null, null, null, 0, null, null, 0, 0);
    }

    static MatchmakingStatusView searching(
        Long selectedBotId,
        String selectedBotName,
        int selectedBotElo,
        String selectedBotDescription,
        long waitSeconds,
        int playersSearching) {
      return new MatchmakingStatusView(
          "searching",
          true,
          false,
          null,
          null,
          selectedBotId,
          selectedBotName,
          selectedBotElo,
          selectedBotDescription,
          null,
          waitSeconds,
          playersSearching);
    }

    static MatchmakingStatusView matched(
        Long matchId,
        String redirectUrl,
        Long selectedBotId,
        String selectedBotName,
        int selectedBotElo,
        String selectedBotDescription,
        String opponentBotName) {
      return new MatchmakingStatusView(
          "matched",
          false,
          true,
          matchId,
          redirectUrl,
          selectedBotId,
          selectedBotName,
          selectedBotElo,
          selectedBotDescription,
          opponentBotName,
          0,
          0);
    }
  }

  public record MatchBattleView(
      Long matchId,
      String bot1Name,
      String bot1Initial,
      String bot2Name,
      String bot2Initial,
      String statsHref) {}

  public record MatchRoundView(
      int roundNumber, String bot1Move, String bot2Move, String result, String resultBadgeClass) {}

  public record MatchStatsView(
      Long matchId,
      String bot1Name,
      String bot2Name,
      String winnerLabel,
      String winnerBadgeClass,
      int bot1Score,
      int bot2Score,
      int totalRounds,
      String playedAt,
      List<MatchRoundView> rounds) {}

  private record SearchTicket(Long userId, String username, Bot bot, LocalDateTime createdAt) {}

  private record ReadyMatch(
      Long userId,
      Long matchId,
      String redirectUrl,
      Long myBotId,
      String myBotName,
      int myBotElo,
      String myBotDescription,
      String opponentBotName,
      LocalDateTime createdAt) {}

  private record RematchInvitation(
      String id,
      Long originalMatchId,
      Long requesterUserId,
      String requesterUsername,
      Bot requesterBot,
      Long opponentUserId,
      String opponentUsername,
      Bot opponentBot,
      Bot originalBot1,
      Bot originalBot2,
      LocalDateTime createdAt) {}

  private record RematchParticipants(Bot requesterBot, Bot opponentBot, User opponentUser) {}

  private enum ParticipationFilter {
    ALL,
    PLAYED,
    NOT_PLAYED;

    static ParticipationFilter fromParam(String value) {
      if (value == null) {
        return ALL;
      }

      String normalizedValue = value.trim().toLowerCase(Locale.ROOT);
      if ("played".equals(normalizedValue)) {
        return PLAYED;
      }
      if ("not-played".equals(normalizedValue) || "not_played".equals(normalizedValue)) {
        return NOT_PLAYED;
      }
      return ALL;
    }
  }
}
