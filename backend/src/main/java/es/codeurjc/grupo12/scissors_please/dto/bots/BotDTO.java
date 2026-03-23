package es.codeurjc.grupo12.scissors_please.dto.bots;

import java.time.LocalDateTime;
import java.util.List;

public record BotDTO(
    Long id,
    String name,
    String description,
    String code,
    boolean isPublic,
    int elo,
    Long ownerId,
    int wins,
    int losses,
    int draws,
    List<String> tags,
    List<Integer> eloHistory,
    boolean hasImage,
    String imageUrl,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
