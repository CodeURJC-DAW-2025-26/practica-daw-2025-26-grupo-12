package es.codeurjc.grupo12.scissors_please.dto.users;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.web.multipart.MultipartFile;

public record UpdateProfileRequestDto(
    @Schema(description = "Editable profile fields") UserUpdateRequestDto request,
    @Schema(
        description = "Profile image file",
        type = "string",
        format = "binary",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        MultipartFile imageFile) {}
