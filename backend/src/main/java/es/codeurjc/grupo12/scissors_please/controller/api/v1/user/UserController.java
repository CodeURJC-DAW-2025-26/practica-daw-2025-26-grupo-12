package es.codeurjc.grupo12.scissors_please.controller.api.v1.user;

import es.codeurjc.grupo12.scissors_please.dto.UserResponseDto;
import es.codeurjc.grupo12.scissors_please.model.Image;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.service.image.ImageService;
import es.codeurjc.grupo12.scissors_please.service.user.UserService;
import java.io.IOException;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController("apiUserController")
@RequestMapping("/api/v1/users")
public class UserController {

  private final UserService userService;
  private final ImageService imageService;
  private final PasswordEncoder passwordEncoder;

  public UserController(
      UserService userService, ImageService imageService, PasswordEncoder passwordEncoder) {
    this.userService = userService;
    this.imageService = imageService;
    this.passwordEncoder = passwordEncoder;
  }

  @PutMapping("/{id}")
  public ResponseEntity<UserResponseDto> updateProfile(
      @PathVariable Long id,
      @RequestPart UserUpdateRequest request,
      @RequestPart("imageFile") MultipartFile imageFile,
      Authentication authentication)
      throws IOException {

    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(401).build();
    }

    User userToUpdate = userService.getUserById(id);
    Image image = imageService.convertToImage(imageFile);

    if (request.username() != null && !request.username().isBlank()) {
      userToUpdate.setUsername(request.username());
    }
    if (request.email() != null && !request.email().isBlank()) {
      userToUpdate.setEmail(request.email());
    }
    if (request.password() != null && !request.password().isBlank()) {
      userToUpdate.setPassword(passwordEncoder.encode(request.password()));
    }
    userToUpdate.setImage(image);

    userService.updateUser(userToUpdate);

    return ResponseEntity.ok(UserResponseDto.from(userToUpdate));
  }

  @PutMapping("/{id}/block")
  public ResponseEntity<UserResponseDto> blockUser(
      @PathVariable Long id, Authentication authentication) {
    String username = authentication.getName();
    Optional<User> adminUser = userService.findByUsername(username);
    if (adminUser.isEmpty()) {
      return ResponseEntity.status(403).build();
    }
    userService.blockUser(id, adminUser.get());
    User updatedUser = userService.getUserById(id);
    return ResponseEntity.ok(UserResponseDto.from(updatedUser));
  }

  @GetMapping("/{id}")
  public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id) {
    User user = userService.getUserById(id);
    return ResponseEntity.ok(UserResponseDto.from(user));
  }

  private record UserUpdateRequest(String username, String email, String password) {}
}
