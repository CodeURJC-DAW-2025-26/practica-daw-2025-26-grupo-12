package es.codeurjc.grupo12.scissors_please.dto.tournaments;

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
    String imageUrl) {
  public static TournamentDto from(Tournament tournament) {
    return new TournamentDto(
        tournament.getId(),
        tournament.getName(),
        tournament.getDescription(),
        tournament.getStatus(),
        tournament.getSlots(),
        tournament.getStartDate(),
        buildImageUrl(tournament));
  }

  private static String buildImageUrl(Tournament tournament) {
    if (tournament == null || tournament.getId() == null || tournament.getImage() == null) {
      return null;
    }
    return "/api/v1/images/tournaments/" + tournament.getId();
  }
}
