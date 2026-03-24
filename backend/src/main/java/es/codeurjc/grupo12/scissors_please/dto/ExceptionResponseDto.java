package es.codeurjc.grupo12.scissors_please.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Respuesta estandar para errores de la API")
public record ExceptionResponseDto(
    @Schema(description = "Mensaje descriptivo del error", example = "Element not found")
        String message,
    @Schema(
            description = "Fecha y hora en la que se produjo el error",
            example = "2026-03-24T10:15:30")
        LocalDateTime timestamp) {}
