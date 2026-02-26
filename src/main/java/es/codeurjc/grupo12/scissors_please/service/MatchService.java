package es.codeurjc.grupo12.scissors_please.service;

import es.codeurjc.grupo12.scissors_please.model.Bot;
import es.codeurjc.grupo12.scissors_please.model.Match;
import es.codeurjc.grupo12.scissors_please.repository.MatchRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MatchService {

  private static final int MAX_PAGE_SIZE = 20;
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  private final MatchRepository matchRepository;

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

  public UserRecentMatchSection getUserRecentMatchSection(Long userId, String participationFilterParam) {
    ParticipationFilter participationFilter = ParticipationFilter.fromParam(participationFilterParam);

    List<Match> allMatches = matchRepository.findAllByOrderByTimestampDesc();
    Set<Long> playedMatchIds =
        matchRepository
            .findDistinctByBot1OwnerIdOrBot2OwnerIdOrderByTimestampDesc(userId, userId)
            .stream()
            .map(Match::getId)
            .collect(Collectors.toSet());

    List<UserMatchItem> matches =
        allMatches.stream()
            .filter(match -> matchesParticipationFilter(match.getId(), playedMatchIds, participationFilter))
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
        "/matches/stats");
  }

  private boolean isOwnedByUser(Bot bot, Long userId) {
    return bot != null && bot.getOwnerId() != null && bot.getOwnerId().equals(userId);
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
        "/matches/stats");
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
