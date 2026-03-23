package es.codeurjc.grupo12.scissors_please.controller.api.v1.user;

import es.codeurjc.grupo12.scissors_please.config.ResponseConstants;
import es.codeurjc.grupo12.scissors_please.dto.ResponseDto;
import es.codeurjc.grupo12.scissors_please.model.Image;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.service.image.ImageService;
import es.codeurjc.grupo12.scissors_please.service.user.UserService;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController("apiUserController")
@RequestMapping("/api/v1/users")
public class UserController {

  @Autowired private UserService userService;
  @Autowired private ImageService imageService;
  @Autowired private PasswordEncoder passwordEncoder;

  @PutMapping("/{id}")
  public ResponseDto updateProfile(
      @PathVariable Long id,
      @RequestPart UserUpdateRequest request,
      @RequestPart("imageFile") MultipartFile imageFile,
      Authentication authentication)
      throws IOException {

    if (authentication == null || !authentication.isAuthenticated()) {
      return new ResponseDto(
          true, ResponseConstants.UNAUTHORIZED_CODE_INT, ResponseConstants.ACCESS_DENIED, null);
    }
    Image image = imageService.convertToImage(imageFile);
    User userToUpdate = userService.getUserById(id);

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

    return new ResponseDto(false, ResponseConstants.OK_CODE_INT, ResponseConstants.OK, null);
  }

  @PutMapping("/{id}/block")
  public ResponseDto BlockUser(@PathVariable Long id, Authentication authentication) {
    String username = authentication.getName();
    Optional<User> adminUser = userService.findByUsername(username);
    if (adminUser.isEmpty()) {
      return new ResponseDto(
          true, ResponseConstants.FORBIDDEN_CODE_INT, ResponseConstants.ELEMENT_NOT_FOUND, null);
    }
    userService.blockUser(id, adminUser.get());
    return new ResponseDto(false, ResponseConstants.OK_CODE_INT, ResponseConstants.OK, null);
  }

  @GetMapping("/{id}")
  public ResponseDto getUserById(@PathVariable Long id) {

    User user = userService.getUserById(id);

    UserResponse response =
        new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getImage() != null ? user.getImage().getId() : null,
            user.getCreatedAt(),
            user.isBlocked());

    return new ResponseDto(false, ResponseConstants.OK_CODE_INT, ResponseConstants.OK, response);
  }

  private record UserUpdateRequest(String username, String email, String password) {}

  private record UserResponse(
      Long id,
      String username,
      String email,
      Long imageId,
      LocalDateTime createdAt,
      boolean blocked) {}
}
