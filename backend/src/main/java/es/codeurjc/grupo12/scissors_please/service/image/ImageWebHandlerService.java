package es.codeurjc.grupo12.scissors_please.service.image;

import es.codeurjc.grupo12.scissors_please.model.Image;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ImageWebHandlerService {

  @Autowired private ImageService imageService;

  public ResponseEntity<byte[]> botImageHandler(Long id) {
    return buildImageResponse(imageService.getBotImageOrThrow(id));
  }

  public ResponseEntity<byte[]> userImageHandler(Long id) {
    return buildImageResponse(imageService.getUserImageOrThrow(id));
  }

  public ResponseEntity<byte[]> tournamentImageHandler(Long id) {
    return buildImageResponse(imageService.getTournamentImageOrThrow(id));
  }

  private ResponseEntity<byte[]> buildImageResponse(Image image) {
    return ResponseEntity.ok()
        .contentType(imageService.resolveMediaType(image))
        .body(image.getData());
  }
}
