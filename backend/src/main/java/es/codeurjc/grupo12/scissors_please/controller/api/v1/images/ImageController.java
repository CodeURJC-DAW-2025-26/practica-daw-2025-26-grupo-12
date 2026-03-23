package es.codeurjc.grupo12.scissors_please.controller.api.v1.images;

import es.codeurjc.grupo12.scissors_please.model.Image;
import es.codeurjc.grupo12.scissors_please.service.image.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("apiImageController")
@RequestMapping("/api/v1/images")
public class ImageController {

  private final ImageService imageService;

  @Autowired
  public ImageController(ImageService imageService) {
    this.imageService = imageService;
  }

  @GetMapping("/bots/{id}")
  public ResponseEntity<byte[]> getBotImage(@PathVariable Long id) {
    return buildImageResponse(imageService.getBotImageOrThrow(id));
  }

  @GetMapping("/users/{id}")
  public ResponseEntity<byte[]> getUserImage(@PathVariable Long id) {
    return buildImageResponse(imageService.getUserImageOrThrow(id));
  }

  @GetMapping("/tournaments/{id}")
  public ResponseEntity<byte[]> getTournamentImage(@PathVariable Long id) {
    return buildImageResponse(imageService.getTournamentImageOrThrow(id));
  }

  private ResponseEntity<byte[]> buildImageResponse(Image image) {
    return ResponseEntity.ok()
        .contentType(imageService.resolveMediaType(image))
        .body(image.getData());
  }
}
