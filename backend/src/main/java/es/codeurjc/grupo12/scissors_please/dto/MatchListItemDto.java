package es.codeurjc.grupo12.scissors_please.dto;

import es.codeurjc.grupo12.scissors_please.views.MatchListItem;

public record MatchListItemDto(
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
    String actionHref) {
  public static MatchListItemDto from(MatchListItem item) {
    return new MatchListItemDto(
        item.id(),
        item.bot1Id(),
        item.bot1Name(),
        item.bot1Initial(),
        item.bot1HasImage(),
        item.bot1OwnerName(),
        item.bot2Id(),
        item.bot2Name(),
        item.bot2Initial(),
        item.bot2HasImage(),
        item.bot2OwnerName(),
        item.topElo(),
        item.result(),
        item.resultBadgeClass(),
        item.date(),
        item.actionHref());
  }
}
