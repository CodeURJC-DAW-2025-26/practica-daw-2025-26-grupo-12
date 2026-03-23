package es.codeurjc.grupo12.scissors_please.dto;

import es.codeurjc.grupo12.scissors_please.model.TournamentStatus;
import java.time.LocalDate;

public record TournamentRequestDto(
    String name,
    String description,
    TournamentStatus status,
    int slots,
    LocalDate registrationStarts,
    LocalDate startDate,
    String price) {}
