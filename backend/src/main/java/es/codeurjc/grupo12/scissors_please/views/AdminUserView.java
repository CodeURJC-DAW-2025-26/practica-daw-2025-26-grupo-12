package es.codeurjc.grupo12.scissors_please.views;

public record AdminUserView(
    Long id,
    String username,
    String email,
    String profileHref,
    String provider,
    boolean blocked,
    boolean manageable,
    boolean adminRole,
    boolean hasImage,
    String initial) {}
