package es.codeurjc.grupo12.scissors_please.service.image;

import es.codeurjc.grupo12.scissors_please.model.Bot;
import es.codeurjc.grupo12.scissors_please.model.Tournament;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.repository.BotRepository;
import es.codeurjc.grupo12.scissors_please.service.tournament.TournamentService;
import es.codeurjc.grupo12.scissors_please.service.user.UserService;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ImageWebHandlerService {

  @Autowired private BotRepository botRepository;
  @Autowired private UserService userService;
  @Autowired private TournamentService tournamentService;

  public ResponseEntity<byte[]> botImageHandler(Long id) {
    Optional<Bot> bot = botRepository.findById(id);
    if (bot.isEmpty() || bot.get().getImage() == null) {
      return ResponseEntity.notFound().build();
    }
    return buildImageResponse(bot.get().getImage().getData());
  }

  public ResponseEntity<byte[]> userImageHandler(Long id) {
    User user;
    try {
      user = userService.getUserById(id);
    } catch (IllegalArgumentException exception) {
      return ResponseEntity.notFound().build();
    }
    if (user.getImage() == null) {
      return ResponseEntity.notFound().build();
    }
    return buildImageResponse(user.getImage().getData());
  }

  public ResponseEntity<byte[]> tournamentImageHandler(Long id) {
    Optional<Tournament> tournament = tournamentService.getTournamentById(id);
    if (tournament.isEmpty() || tournament.get().getImage() == null) {
      return ResponseEntity.notFound().build();
    }
    return buildImageResponse(tournament.get().getImage().getData());
  }

  private ResponseEntity<byte[]> buildImageResponse(byte[] imageBytes) {
    String mimeType = null;
    try {
      mimeType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(imageBytes));
    } catch (IOException exception) {
      // Fall back to JPEG when the content type cannot be detected.
    }

    MediaType contentType =
        mimeType != null ? MediaType.parseMediaType(mimeType) : MediaType.IMAGE_JPEG;
    return ResponseEntity.ok().contentType(contentType).body(imageBytes);
  }
}
