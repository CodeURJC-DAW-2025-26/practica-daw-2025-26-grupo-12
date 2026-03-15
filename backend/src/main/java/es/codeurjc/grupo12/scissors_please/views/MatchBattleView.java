package es.codeurjc.grupo12.scissors_please.views;

public record MatchBattleView(
    Long matchId,
    Long bot1Id,
    String bot1Name,
    String bot1Initial,
    boolean bot1HasImage,
    String bot1OwnerName,
    Long bot2Id,
    String bot2Name,
    String bot2Initial,
    boolean bot2HasImage,
    String bot2OwnerName,
    String statsHref) {}
