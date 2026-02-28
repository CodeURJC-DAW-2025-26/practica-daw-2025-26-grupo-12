package es.codeurjc.grupo12.scissors_please.service;

import es.codeurjc.grupo12.scissors_please.model.Bot;
import es.codeurjc.grupo12.scissors_please.model.Match;
import es.codeurjc.grupo12.scissors_please.model.Round;
import es.codeurjc.grupo12.scissors_please.repository.BotRepository;
import es.codeurjc.grupo12.scissors_please.repository.MatchRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class MatchService {

  private static final int MAX_PAGE_SIZE = 20;
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
  private static final List<String> MOVES = List.of("Rock", "Paper", "Scissors");

  @Autowired private MatchRepository matchRepository;
  @Autowired private BotRepository botRepository;

  public MatchStartResult startMatchmaking(Long userId, Long selectedBotId) {
    Bot myBot = resolveMyBot(userId, selectedBotId);
    Bot opponentBot = resolveOpponentBot(userId, myBot);

    Match match = new Match();
    match.setBot1(myBot);
    match.setBot2(opponentBot);
    match.setTimestamp(LocalDateTime.now());
    populateRandomResult(match);

    Match savedMatch = matchRepository.save(match);
    return new MatchStartResult(
        savedMatch.getId(), resolveBotName(myBot), resolveBotName(opponentBot));
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
            .sorted(java.util.Comparator.comparingInt(Round::getRoundNumber))
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
      throw new IllegalArgumentException("You need at least one bot to start a match.");
    }

    if (selectedBotId != null) {
      return userBots.stream()
          .filter(bot -> selectedBotId.equals(bot.getId()))
          .findFirst()
          .orElseThrow(() -> new IllegalArgumentException("Selected bot is not available."));
    }

    return userBots.stream().max(java.util.Comparator.comparingInt(Bot::getElo)).orElseThrow();
  }

  private Bot resolveOpponentBot(Long userId, Bot myBot) {
    List<Bot> candidates =
        botRepository.findByIsPublicTrue().stream()
            .filter(bot -> bot.getOwnerId() != null && !bot.getOwnerId().equals(userId))
            .filter(bot -> myBot.getId() == null || !myBot.getId().equals(bot.getId()))
            .toList();

    if (candidates.isEmpty()) {
      candidates =
          botRepository.findAll().stream()
              .filter(bot -> bot.getOwnerId() != null && !bot.getOwnerId().equals(userId))
              .filter(bot -> myBot.getId() == null || !myBot.getId().equals(bot.getId()))
              .toList();
    }

    if (candidates.isEmpty()) {
      throw new IllegalArgumentException("No opponents available right now.");
    }

    List<Bot> sortedByEloDistance =
        candidates.stream()
            .sorted(
                java.util.Comparator.comparingInt(
                    bot -> Math.abs(resolveBotElo(bot) - resolveBotElo(myBot))))
            .toList();
    int poolSize = Math.min(3, sortedByEloDistance.size());
    int randomIndex = ThreadLocalRandom.current().nextInt(poolSize);
    return sortedByEloDistance.get(randomIndex);
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

  public record MatchStartResult(Long matchId, String myBotName, String opponentBotName) {}

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
