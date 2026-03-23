package es.codeurjc.grupo12.scissors_please.dto;

public record MatchmakingStatusDto(
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

  public static MatchmakingStatusDto idle() {
    return new MatchmakingStatusDto(
        "idle", false, false, null, null, null, null, 0, null, null, 0, 0);
  }

  public static MatchmakingStatusDto searching(
      Long selectedBotId,
      String selectedBotName,
      int selectedBotElo,
      String selectedBotDescription,
      long waitSeconds,
      int playersSearching) {
    return new MatchmakingStatusDto(
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

  public static MatchmakingStatusDto matched(
      Long matchId,
      String redirectUrl,
      Long selectedBotId,
      String selectedBotName,
      int selectedBotElo,
      String selectedBotDescription,
      String opponentBotName) {
    return new MatchmakingStatusDto(
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
