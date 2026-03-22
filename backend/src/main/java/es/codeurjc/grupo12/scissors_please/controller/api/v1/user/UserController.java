package es.codeurjc.grupo12.scissors_please.controller.api.v1.user;

import es.codeurjc.grupo12.scissors_please.config.ResponseConstants;
import es.codeurjc.grupo12.scissors_please.dto.ResponseDto;
import es.codeurjc.grupo12.scissors_please.model.Image;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.service.image.ImageService;
import es.codeurjc.grupo12.scissors_please.service.user.UserService;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
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
      @RequestPart("imageFile") MultipartFile imageFile)
      throws IOException {

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

  private record UserUpdateRequest(String username, String email, String password) {}
}
