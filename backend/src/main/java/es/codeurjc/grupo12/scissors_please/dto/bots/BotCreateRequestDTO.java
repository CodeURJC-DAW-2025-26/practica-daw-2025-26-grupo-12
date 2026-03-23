package es.codeurjc.grupo12.scissors_please.dto.bots;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class BotCreateRequestDTO {
  private String name;
  private String description;
  private String tags;
  private boolean isPublic;
  private MultipartFile imageFile;
}
