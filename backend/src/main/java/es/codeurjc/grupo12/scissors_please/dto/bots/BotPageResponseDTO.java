package es.codeurjc.grupo12.scissors_please.dto.bots;

import java.util.List;

public record BotPageResponseDTO(
    List<BotDTOWithSimpleImage> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean hasNext,
    boolean hasPrevious,
    boolean first,
    boolean last) {}
