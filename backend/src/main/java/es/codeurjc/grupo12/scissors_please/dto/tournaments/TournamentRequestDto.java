package es.codeurjc.grupo12.scissors_please.dto.tournaments;

import java.time.LocalDate;

public record TournamentRequestDto(
    String name,
    String description,
    String status,
    int slots,
    LocalDate registrationStarts,
    LocalDate startDate,
    String price) {}
