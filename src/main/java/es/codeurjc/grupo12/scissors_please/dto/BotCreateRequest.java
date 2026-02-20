package es.codeurjc.grupo12.scissors_please.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class BotCreateRequest {
  public static final int MAX_NAME_LENGTH = 60;
  public static final int MAX_DESCRIPTION_LENGTH = 500;
  public static final int MAX_LANGUAGE_LENGTH = 30;
  public static final int MAX_CODE_LENGTH = 4000;
  public static final int MAX_IMAGE_LENGTH = 255;
  public static final int MAX_TAGS = 10;
  public static final int MAX_TAG_LENGTH = 30;

  @NotBlank(message = "El nombre del bot es obligatorio")
  @Size(
      max = MAX_NAME_LENGTH,
      message = "El nombre no puede superar " + MAX_NAME_LENGTH + " caracteres")
  private String name;

  @Size(
      max = MAX_DESCRIPTION_LENGTH,
      message = "La descripción no puede superar " + MAX_DESCRIPTION_LENGTH + " caracteres")
  private String description;

  @Size(
      max = MAX_LANGUAGE_LENGTH,
      message = "El lenguaje no puede superar " + MAX_LANGUAGE_LENGTH + " caracteres")
  private String language;

  @Size(
      max = MAX_CODE_LENGTH,
      message = "El código no puede superar " + MAX_CODE_LENGTH + " caracteres")
  private String code;

  @Size(
      max = MAX_IMAGE_LENGTH,
      message = "La imagen no puede superar " + MAX_IMAGE_LENGTH + " caracteres")
  private String image;

  private boolean isPublic;

  @Size(max = MAX_TAGS, message = "No puedes añadir más de " + MAX_TAGS + " tags")
  private List<
          @Size(
              max = MAX_TAG_LENGTH,
              message = "Cada tag debe tener máximo " + MAX_TAG_LENGTH + " caracteres")
          String>
      tags = new ArrayList<>();
}
