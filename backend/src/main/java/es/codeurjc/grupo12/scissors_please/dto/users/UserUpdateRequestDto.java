package es.codeurjc.grupo12.scissors_please.dto.users;

import io.swagger.v3.oas.annotations.media.Schema;

public record UserUpdateRequestDto(
    @Schema(description = "New username", example = "new-user") String username,
    @Schema(description = "New email address", example = "new-user@example.com") String email,
    @Schema(description = "New raw password", example = "secret123") String password) {}
