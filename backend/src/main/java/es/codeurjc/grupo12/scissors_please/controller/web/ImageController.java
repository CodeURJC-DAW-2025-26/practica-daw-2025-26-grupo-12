package es.codeurjc.grupo12.scissors_please.controller.web;

import es.codeurjc.grupo12.scissors_please.service.image.ImageWebHandlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ImageController {

  @Autowired private ImageWebHandlerService imageWebHandlerService;

  @GetMapping("/bot-images/{id}")
  public ResponseEntity<byte[]> getBotImage(@PathVariable Long id) {
    return imageWebHandlerService.botImageHandler(id);
  }

  @GetMapping("/user-images/{id}")
  public ResponseEntity<byte[]> getUserImage(@PathVariable Long id) {
    return imageWebHandlerService.userImageHandler(id);
  }

  @GetMapping("/tournament-images/{id}")
  public ResponseEntity<byte[]> getTournamentImage(@PathVariable Long id) {
    return imageWebHandlerService.tournamentImageHandler(id);
  }
}
