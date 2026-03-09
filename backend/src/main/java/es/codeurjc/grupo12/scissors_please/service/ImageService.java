package es.codeurjc.grupo12.scissors_please.service;

import es.codeurjc.grupo12.scissors_please.model.Bot;
import es.codeurjc.grupo12.scissors_please.model.Image;
import java.io.IOException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageService {

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
}
