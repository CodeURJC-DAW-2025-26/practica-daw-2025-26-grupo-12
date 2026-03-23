package es.codeurjc.grupo12.scissors_please.dto;

import es.codeurjc.grupo12.scissors_please.model.User;
import java.time.LocalDateTime;

public record UserResponseDto(
    Long id,
    String username,
    String email,
    String imageUrl,
    LocalDateTime createdAt,
    boolean blocked) {
  public static UserResponseDto from(User user) {
    return new UserResponseDto(
        user.getId(),
        user.getUsername(),
        user.getEmail(),
        buildImageUrl(user),
        user.getCreatedAt(),
        user.isBlocked());
  }

  private static String buildImageUrl(User user) {
    if (user == null || user.getId() == null || user.getImage() == null) {
      return null;
    }
    return "/api/v1/images/users/" + user.getId();
  }
}
