package es.codeurjc.grupo12.scissors_please.dto;

import es.codeurjc.grupo12.scissors_please.model.Match;
import java.time.LocalDateTime;

public record MatchDto(
    Long id,
    String bot1Name,
    String bot2Name,
    int bot1Score,
    int bot2Score,
    String result,
    LocalDateTime timestamp) {
  public static MatchDto from(Match match) {
    return new MatchDto(
        match.getId(),
        match.getBot1() != null ? match.getBot1().getName() : null,
        match.getBot2() != null ? match.getBot2().getName() : null,
        match.getBot1Score(),
        match.getBot2Score(),
        match.getResult(),
        match.getTimestamp());
  }
}