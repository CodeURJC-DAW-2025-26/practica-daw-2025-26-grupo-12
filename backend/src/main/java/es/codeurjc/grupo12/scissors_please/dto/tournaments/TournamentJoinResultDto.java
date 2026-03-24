package es.codeurjc.grupo12.scissors_please.dto.tournaments;

import es.codeurjc.grupo12.scissors_please.views.JoinTournamentResult;
import es.codeurjc.grupo12.scissors_please.views.JoinTournamentStatus;

public record TournamentJoinResultDto(JoinTournamentStatus status, String message) {

  public static TournamentJoinResultDto from(JoinTournamentResult result) {
    return new TournamentJoinResultDto(result.status(), result.message());
  }
}
