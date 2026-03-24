package es.codeurjc.grupo12.scissors_please.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

public record RegisterRequest(
    @Schema(description = "Unique username for the new account", example = "jane.doe")
        String username,
    @Schema(description = "Password for the new account", example = "StrongPass123!")
        String password,
    @Schema(description = "Email address for the new account", example = "jane@example.com")
        String email) {}
