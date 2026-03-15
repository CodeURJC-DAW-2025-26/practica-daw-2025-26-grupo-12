package es.codeurjc.grupo12.scissors_please.views;

import es.codeurjc.grupo12.scissors_please.model.TournamentStatus;
import java.time.LocalDate;

public record AdminTournamentDetail(
    Long id,
    String name,
    String description,
    TournamentStatus status,
    LocalDate startDate,
    int slots,
    int participants,
    boolean canRunNow,
    boolean hasImage) {}
