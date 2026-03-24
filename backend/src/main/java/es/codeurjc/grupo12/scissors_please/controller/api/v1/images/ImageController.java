package es.codeurjc.grupo12.scissors_please.controller.api.v1.images;

import es.codeurjc.grupo12.scissors_please.dto.ExceptionResponseDto;
import es.codeurjc.grupo12.scissors_please.model.Image;
import es.codeurjc.grupo12.scissors_please.service.image.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("apiImageController")
@RequestMapping("/api/v1/images")
@Tag(name = "Images", description = "Binary image retrieval endpoints")
public class ImageController {

  private final ImageService imageService;

  @Autowired
  public ImageController(ImageService imageService) {
    this.imageService = imageService;
  }

  @GetMapping("/bots/{id}")
  @Operation(
      summary = "Get a bot image",
      description = "Returns the raw image bytes associated with the specified bot.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Image returned successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    schema = @Schema(type = "string", format = "binary"))),
        @ApiResponse(
            responseCode = "404",
            description = "Bot image not found",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ExceptionResponseDto.class)))
      })
  public ResponseEntity<byte[]> getBotImage(@PathVariable Long id) {
    return buildImageResponse(imageService.getBotImageOrThrow(id));
  }

  @GetMapping("/users/{id}")
  @Operation(
      summary = "Get a user image",
      description = "Returns the raw image bytes associated with the specified user.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Image returned successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    schema = @Schema(type = "string", format = "binary"))),
        @ApiResponse(
            responseCode = "404",
            description = "User image not found",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ExceptionResponseDto.class)))
      })
  public ResponseEntity<byte[]> getUserImage(@PathVariable Long id) {
    return buildImageResponse(imageService.getUserImageOrThrow(id));
  }

  @GetMapping("/tournaments/{id}")
  @Operation(
      summary = "Get a tournament image",
      description = "Returns the raw image bytes associated with the specified tournament.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Image returned successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    schema = @Schema(type = "string", format = "binary"))),
        @ApiResponse(
            responseCode = "404",
            description = "Tournament image not found",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ExceptionResponseDto.class)))
      })
  public ResponseEntity<byte[]> getTournamentImage(@PathVariable Long id) {
    return buildImageResponse(imageService.getTournamentImageOrThrow(id));
  }

  private ResponseEntity<byte[]> buildImageResponse(Image image) {
    return ResponseEntity.ok()
        .contentType(imageService.resolveMediaType(image))
        .body(image.getData());
  }
}
