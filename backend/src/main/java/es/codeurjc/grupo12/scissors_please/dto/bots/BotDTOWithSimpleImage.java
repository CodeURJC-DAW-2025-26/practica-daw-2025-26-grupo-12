package es.codeurjc.grupo12.scissors_please.dto.bots;

import java.time.LocalDateTime;
import java.util.List;

public record BotDTOWithSimpleImage(
    Long id,
    String name,
    String description,
    boolean isPublic,
    int elo,
    Long ownerId,
    int wins,
    int losses,
    int draws,
    List<String> tags,
    boolean hasImage,
    String imageUrl,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
