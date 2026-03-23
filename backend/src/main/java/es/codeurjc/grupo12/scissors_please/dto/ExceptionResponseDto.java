package es.codeurjc.grupo12.scissors_please.dto;

import java.time.LocalDateTime;

public record ExceptionResponseDto(String message, LocalDateTime timestamp) {}
