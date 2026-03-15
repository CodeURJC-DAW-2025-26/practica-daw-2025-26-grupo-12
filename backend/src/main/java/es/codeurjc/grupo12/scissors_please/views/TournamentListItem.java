package es.codeurjc.grupo12.scissors_please.views;

public record TournamentListItem(
    Long id,
    String name,
    String summary,
    String status,
    String badgeClass,
    String actionLabel,
    String actionHref,
    boolean actionDisabled,
    boolean hasImage,
    int occupiedSlots,
    int totalSlots) {}
