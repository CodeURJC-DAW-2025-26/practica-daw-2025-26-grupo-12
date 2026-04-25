package es.codeurjc.grupo12.scissors_please.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

public record AdminStatusResponse(
    @Schema(
            description = "Whether the current authenticated user has the admin role",
            example = "true")
        boolean admin) {}
