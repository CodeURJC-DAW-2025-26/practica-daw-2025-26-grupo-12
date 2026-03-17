package es.codeurjc.grupo12.scissors_please.controller.web;

import es.codeurjc.grupo12.scissors_please.service.image.ImageWebHandlerService;
import es.codeurjc.grupo12.scissors_please.views.BinaryResponseView;
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
    BinaryResponseView view = imageWebHandlerService.botImageHandler(id);
    return view.responseEntity();
  }

  @GetMapping("/user-images/{id}")
  public ResponseEntity<byte[]> getUserImage(@PathVariable Long id) {
    BinaryResponseView view = imageWebHandlerService.userImageHandler(id);
    return view.responseEntity();
  }

  @GetMapping("/tournament-images/{id}")
  public ResponseEntity<byte[]> getTournamentImage(@PathVariable Long id) {
    BinaryResponseView view = imageWebHandlerService.tournamentImageHandler(id);
    return view.responseEntity();
  }
}
