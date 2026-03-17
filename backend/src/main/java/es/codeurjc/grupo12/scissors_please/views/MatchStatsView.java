package es.codeurjc.grupo12.scissors_please.views;

import java.util.List;

public record MatchStatsView(
    Long matchId,
    Long bot1Id,
    String bot1Name,
    String bot1OwnerName,
    Long bot2Id,
    String bot2Name,
    String bot2OwnerName,
    String winnerLabel,
    String winnerBadgeClass,
    int bot1Score,
    int bot2Score,
    int totalRounds,
    String playedAt,
    List<MatchRoundView> rounds) {}
