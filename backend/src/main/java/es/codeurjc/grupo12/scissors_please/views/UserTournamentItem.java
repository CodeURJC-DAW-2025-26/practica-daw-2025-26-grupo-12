package es.codeurjc.grupo12.scissors_please.views;

public record UserTournamentItem(
    Long id,
    String name,
    String date,
    String format,
    String status,
    String statusBadgeClass,
    String actionLabel,
    String actionHref,
    boolean actionDisabled,
    boolean hasImage) {}
