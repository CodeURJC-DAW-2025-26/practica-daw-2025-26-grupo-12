package es.codeurjc.grupo12.scissors_please.dto.matches;

public record MatchRoundDto(
    int roundNumber, String bot1Move, String bot2Move, String result, String resultBadgeClass) {}
