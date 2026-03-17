package es.codeurjc.grupo12.scissors_please.views;

public record MatchRoundView(
    int roundNumber, String bot1Move, String bot2Move, String result, String resultBadgeClass) {}
