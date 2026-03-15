package es.codeurjc.grupo12.scissors_please.views;

public record MatchListItem(
    Long id,
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
    int topElo,
    String result,
    String resultBadgeClass,
    String date,
    String actionHref) {}
