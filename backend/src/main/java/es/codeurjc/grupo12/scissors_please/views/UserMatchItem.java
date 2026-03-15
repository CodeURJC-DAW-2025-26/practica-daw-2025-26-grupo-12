package es.codeurjc.grupo12.scissors_please.views;

public record UserMatchItem(
    Long id,
    Long myBotId,
    String myBotName,
    boolean myBotHasImage,
    Long opponentBotId,
    String opponentName,
    String opponentOwnerName,
    boolean opponentHasImage,
    String result,
    String resultBadgeClass,
    String date,
    String actionHref) {}
