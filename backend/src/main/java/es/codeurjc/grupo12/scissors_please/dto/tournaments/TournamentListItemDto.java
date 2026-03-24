package es.codeurjc.grupo12.scissors_please.dto.tournaments;

import es.codeurjc.grupo12.scissors_please.views.TournamentListItem;

public record TournamentListItemDto(
    Long id, String name, int slots, int registered, String status) {
  public static TournamentListItemDto from(TournamentListItem item) {
    return new TournamentListItemDto(
        item.id(), item.name(), item.totalSlots(), item.occupiedSlots(), item.status());
  }
}
