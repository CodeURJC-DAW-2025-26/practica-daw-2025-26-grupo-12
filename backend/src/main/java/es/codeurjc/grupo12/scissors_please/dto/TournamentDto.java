package es.codeurjc.grupo12.scissors_please.dto;

import es.codeurjc.grupo12.scissors_please.model.Tournament;
import es.codeurjc.grupo12.scissors_please.model.TournamentStatus;
import java.time.LocalDate;

public record TournamentDto(
    Long id,
    String name,
    String description,
    TournamentStatus status,
    int slots,
    LocalDate startDate,
    Long imageId) {
  public static TournamentDto from(Tournament tournament) {
    return new TournamentDto(
        tournament.getId(),
        tournament.getName(),
        tournament.getDescription(),
        tournament.getStatus(),
        tournament.getSlots(),
        tournament.getStartDate(),
        tournament.getImage() != null ? tournament.getImage().getId() : null);
  }
}
