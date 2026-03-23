package es.codeurjc.grupo12.scissors_please.dto;

import es.codeurjc.grupo12.scissors_please.model.User;
import java.time.LocalDateTime;

public record UserResponseDto(
    Long id,
    String username,
    String email,
    Long imageId,
    LocalDateTime createdAt,
    boolean blocked) {
  public static UserResponseDto from(User user) {
    return new UserResponseDto(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        user.getImage() != null ? user.getImage().getId() : null,
        user.getCreatedAt(),
        user.isBlocked());
  }
}
