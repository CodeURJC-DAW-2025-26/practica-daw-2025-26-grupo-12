package es.codeurjc.grupo12.scissors_please.controller.web;

import es.codeurjc.grupo12.scissors_please.model.Bot;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.service.BotService;
import es.codeurjc.grupo12.scissors_please.service.UserService;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ImageController {

  @Autowired private BotService botService;
  @Autowired private UserService userService;

  @GetMapping("/bot-images/{id}")
  public ResponseEntity<byte[]> getBotImage(@PathVariable Long id) {
    Optional<Bot> opBot = botService.getBotById(id);

    if (opBot.isPresent() && opBot.get().getImage() != null) {
      byte[] imageBytes = opBot.get().getImage().getData();

      String mimeType = null;
      try {
        mimeType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(imageBytes));
      } catch (IOException e) {
      }

      MediaType contentType =
          (mimeType != null) ? MediaType.parseMediaType(mimeType) : MediaType.IMAGE_JPEG;

      return ResponseEntity.ok().contentType(contentType).body(imageBytes);
    }

    return ResponseEntity.notFound().build();
  }

  @GetMapping("/user-images/{id}")
  public ResponseEntity<byte[]> getUserImage(@PathVariable Long id) {
    User user = userService.getUserById(id);

    if (user != null && user.getImage() != null) {
      byte[] imageBytes = user.getImage().getData();

      String mimeType = null;
      try {
        mimeType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(imageBytes));
      } catch (IOException e) {
      }

      MediaType contentType =
          (mimeType != null) ? MediaType.parseMediaType(mimeType) : MediaType.IMAGE_JPEG;

      return ResponseEntity.ok().contentType(contentType).body(imageBytes);
    }

    return ResponseEntity.notFound().build();
  }
}
