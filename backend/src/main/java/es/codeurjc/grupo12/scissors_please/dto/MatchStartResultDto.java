package es.codeurjc.grupo12.scissors_please.dto;

public record MatchStartResultDto(
    boolean matched, Long matchId, String myBotName, String opponentBotName, boolean searching) {

  public static MatchStartResultDto matched(
      Long matchId, String myBotName, String opponentBotName) {
    return new MatchStartResultDto(true, matchId, myBotName, opponentBotName, false);
  }

  public static MatchStartResultDto searching(String myBotName) {
    return new MatchStartResultDto(false, null, myBotName, null, true);
  }
}
