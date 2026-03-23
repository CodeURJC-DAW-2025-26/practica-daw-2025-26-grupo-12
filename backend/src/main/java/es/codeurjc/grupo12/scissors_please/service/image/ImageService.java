package es.codeurjc.grupo12.scissors_please.service.image;

import es.codeurjc.grupo12.scissors_please.config.ResponseConstants;
import es.codeurjc.grupo12.scissors_please.exception.ImageNotFoundException;
import es.codeurjc.grupo12.scissors_please.model.Bot;
import es.codeurjc.grupo12.scissors_please.model.Image;
import es.codeurjc.grupo12.scissors_please.model.Tournament;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.repository.BotRepository;
import es.codeurjc.grupo12.scissors_please.repository.TournamentRepository;
import es.codeurjc.grupo12.scissors_please.repository.UserRepository;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageService {

  @Autowired private BotRepository botRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private TournamentRepository tournamentRepository;

  public boolean handleImageUpload(Bot bot, MultipartFile imageFile) {
    if (imageFile == null || imageFile.isEmpty()) {
      return true;
    }

    String contentType = imageFile.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      return false;
    }

    try {
      Image img = new Image();
      img.setFilename(imageFile.getOriginalFilename());
      img.setContentType(contentType);
      img.setData(imageFile.getBytes());

      bot.setImage(img);
      return true;

    } catch (IOException e) {
      return false;
    }
  }

  public Image convertToImage(MultipartFile file) throws IOException {
    if (file == null || file.isEmpty()) {
      return null;
    }

    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      throw new IllegalArgumentException();
    }

    Image img = new Image();
    img.setFilename(file.getOriginalFilename());
    img.setContentType(contentType);
    img.setData(file.getBytes());

    return img;
  }

  public Image getBotImageOrThrow(Long id) {
    Bot bot =
        botRepository
            .findById(id)
            .orElseThrow(() -> new ImageNotFoundException(ResponseConstants.IMAGE_NOT_FOUND));
    return requireImage(bot.getImage());
  }

  public Image getUserImageOrThrow(Long id) {
    User user =
        userRepository
            .findById(id)
            .orElseThrow(() -> new ImageNotFoundException(ResponseConstants.IMAGE_NOT_FOUND));
    return requireImage(user.getImage());
  }

  public Image getTournamentImageOrThrow(Long id) {
    Tournament tournament =
        tournamentRepository
            .findById(id)
            .orElseThrow(() -> new ImageNotFoundException(ResponseConstants.IMAGE_NOT_FOUND));
    return requireImage(tournament.getImage());
  }

  public MediaType resolveMediaType(Image image) {
    String contentType = image != null ? image.getContentType() : null;
    if (contentType != null && !contentType.isBlank()) {
      return MediaType.parseMediaType(contentType);
    }

    byte[] imageBytes = image != null ? image.getData() : null;
    if (imageBytes != null) {
      try {
        String guessedType =
            URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(imageBytes));
        if (guessedType != null && !guessedType.isBlank()) {
          return MediaType.parseMediaType(guessedType);
        }
      } catch (IOException exception) {
        // This should not break
      }
    }

    return MediaType.IMAGE_JPEG;
  }

  private Image requireImage(Image image) {
    if (image == null) {
      throw new ImageNotFoundException(ResponseConstants.IMAGE_NOT_FOUND);
    }
    return image;
  }
}
