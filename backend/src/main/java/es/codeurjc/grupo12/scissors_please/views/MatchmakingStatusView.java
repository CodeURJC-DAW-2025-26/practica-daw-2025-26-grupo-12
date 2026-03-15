package es.codeurjc.grupo12.scissors_please.views;

public record MatchmakingStatusView(
    String state,
    boolean searching,
    boolean matched,
    Long matchId,
    String redirectUrl,
    Long selectedBotId,
    String selectedBotName,
    int selectedBotElo,
    String selectedBotDescription,
    String opponentBotName,
    long waitSeconds,
    int playersSearching) {

  public static MatchmakingStatusView idle() {
    return new MatchmakingStatusView(
        "idle", false, false, null, null, null, null, 0, null, null, 0, 0);
  }

  public static MatchmakingStatusView searching(
      Long selectedBotId,
      String selectedBotName,
      int selectedBotElo,
      String selectedBotDescription,
      long waitSeconds,
      int playersSearching) {
    return new MatchmakingStatusView(
        "searching",
        true,
        false,
        null,
        null,
        selectedBotId,
        selectedBotName,
        selectedBotElo,
        selectedBotDescription,
        null,
        waitSeconds,
        playersSearching);
  }

  public static MatchmakingStatusView matched(
      Long matchId,
      String redirectUrl,
      Long selectedBotId,
      String selectedBotName,
      int selectedBotElo,
      String selectedBotDescription,
      String opponentBotName) {
    return new MatchmakingStatusView(
        "matched",
        false,
        true,
        matchId,
        redirectUrl,
        selectedBotId,
        selectedBotName,
        selectedBotElo,
        selectedBotDescription,
        opponentBotName,
        0,
        0);
  }
}
