package es.codeurjc.grupo12.scissors_please.dto.tournaments;

import java.time.LocalDate;

public record TournamentCreateRequestDto(
    String name,
    String description,
    int slots,
    LocalDate registrationStarts,
    LocalDate startDate,
    String price) {}
