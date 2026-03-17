package es.codeurjc.grupo12.scissors_please.views;

public record MatchStartResult(
    boolean matched, Long matchId, String myBotName, String opponentBotName, boolean searching) {

  public static MatchStartResult matched(Long matchId, String myBotName, String opponentBotName) {
    return new MatchStartResult(true, matchId, myBotName, opponentBotName, false);
  }

  public static MatchStartResult searching(String myBotName) {
    return new MatchStartResult(false, null, myBotName, null, true);
  }
}
