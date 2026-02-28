package es.codeurjc.grupo12.scissors_please.dto;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

  @NotBlank(message = "Bot name is required")
  @Size(
      max = MAX_NAME_LENGTH,
      message = "Bot name cannot exceed " + MAX_NAME_LENGTH + " characters")
  private String name;

  @Size(
      max = MAX_DESCRIPTION_LENGTH,
      message = "Description cannot exceed " + MAX_DESCRIPTION_LENGTH + " characters")
  private String description;

  @Size(
      max = MAX_LANGUAGE_LENGTH,
      message = "Language cannot exceed " + MAX_LANGUAGE_LENGTH + " characters")
  private String language;

  @Size(max = MAX_CODE_LENGTH, message = "Code cannot exceed " + MAX_CODE_LENGTH + " characters")
  private String code;

  @Size(max = MAX_IMAGE_LENGTH, message = "Image cannot exceed " + MAX_IMAGE_LENGTH + " characters")
  private String image;

  private boolean isPublic;

  @Size(max = MAX_TAGS, message = "You cannot add more than " + MAX_TAGS + " tags")
  private List<
          @Size(
              max = MAX_TAG_LENGTH,
              message = "Each tag must be at most " + MAX_TAG_LENGTH + " characters")
          String>
      tags = new ArrayList<>();
}
