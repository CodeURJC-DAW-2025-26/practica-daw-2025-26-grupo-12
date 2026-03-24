package es.codeurjc.grupo12.scissors_please.controller.api.v1.user;

import es.codeurjc.grupo12.scissors_please.dto.ExceptionResponseDto;
import es.codeurjc.grupo12.scissors_please.dto.UserPageResponseDto;
import es.codeurjc.grupo12.scissors_please.dto.UserResponseDto;
import es.codeurjc.grupo12.scissors_please.model.Image;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.service.image.ImageService;
import es.codeurjc.grupo12.scissors_please.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController("apiUserController")
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "Operations to list, inspect and update users")
public class UserController {

  private static final int DEFAULT_PAGE_SIZE = 10;

  private final UserService userService;
  private final ImageService imageService;
  private final PasswordEncoder passwordEncoder;

  public UserController(
      UserService userService, ImageService imageService, PasswordEncoder passwordEncoder) {
    this.userService = userService;
    this.imageService = imageService;
    this.passwordEncoder = passwordEncoder;
  }

  @GetMapping
  @Operation(
      summary = "List active users",
      description = "Returns a paginated list of active users, optionally filtered by username.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Users page returned successfully",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = UserPageResponseDto.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid pagination parameters",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ExceptionResponseDto.class)))
  })
  public ResponseEntity<UserPageResponseDto> getUsers(
      @Parameter(description = "Optional username filter", example = "alice")
          @RequestParam(value = "query", required = false)
          String query,
      @Parameter(description = "Page number starting at zero", example = "0")
          @RequestParam(value = "page", defaultValue = "0")
          int page,
      @Parameter(description = "Page size", example = "10")
          @RequestParam(value = "size", defaultValue = "10")
          int size) {
    PageRequest pageable = PageRequest.of(sanitizePage(page), sanitizeSize(size));
    Page<UserResponseDto> userPage =
        userService.getActiveUserPageByUsername(query, pageable).map(UserResponseDto::from);

    return ResponseEntity.ok(UserPageResponseDto.fromPage(userPage));
  }

  @PutMapping("/{id}")
  @Operation(
      summary = "Update a user profile",
      description =
          "Updates the username, email, password and profile image for the user identified by id.",
      requestBody =
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              required = true,
              content =
                  @Content(
                      mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                      schema = @Schema(implementation = UpdateProfileRequest.class))))
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Profile updated successfully",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = UserResponseDto.class))),
    @ApiResponse(
        responseCode = "400",
        description = "Invalid multipart payload or image upload failed",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ExceptionResponseDto.class))),
    @ApiResponse(
        responseCode = "401",
        description = "Authentication is required",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ExceptionResponseDto.class))),
    @ApiResponse(
        responseCode = "404",
        description = "User not found",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ExceptionResponseDto.class)))
  })
  public ResponseEntity<UserResponseDto> updateProfile(
      @Parameter(description = "Identifier of the user to update", example = "1")
          @PathVariable
          Long id,
      @Parameter(
              name = "request",
              description = "Multipart part with the editable profile fields serialized as JSON",
              required = true,
              content = @Content(schema = @Schema(implementation = UserUpdateRequest.class)))
          @RequestPart("request")
          UserUpdateRequest request,
      @Parameter(
              name = "imageFile",
              description = "New profile image",
              required = true,
              content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE))
          @RequestPart("imageFile")
          MultipartFile imageFile,
      @Parameter(hidden = true) Authentication authentication)
      throws IOException {

    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(401).build();
    }

    User userToUpdate = userService.getUserById(id);
    Image image = imageService.convertToImage(imageFile);

    if (request.username() != null && !request.username().isBlank()) {
      userToUpdate.setUsername(request.username());
    }
    if (request.email() != null && !request.email().isBlank()) {
      userToUpdate.setEmail(request.email());
    }
    if (request.password() != null && !request.password().isBlank()) {
      userToUpdate.setPassword(passwordEncoder.encode(request.password()));
    }
    userToUpdate.setImage(image);

    userService.updateUser(userToUpdate);

    return ResponseEntity.ok(UserResponseDto.from(userToUpdate));
  }

  @PutMapping("/{id}/block")
  @Operation(
      summary = "Block a user",
      description = "Blocks the user identified by id using the authenticated admin user.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "User blocked successfully",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = UserResponseDto.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Current user is not allowed to block users",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ExceptionResponseDto.class))),
    @ApiResponse(
        responseCode = "404",
        description = "User not found",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ExceptionResponseDto.class)))
  })
  public ResponseEntity<UserResponseDto> blockUser(
      @Parameter(description = "Identifier of the user to block", example = "1")
          @PathVariable
          Long id,
      @Parameter(hidden = true) Authentication authentication) {
    String username = authentication.getName();
    Optional<User> adminUser = userService.findByUsername(username);
    if (adminUser.isEmpty()) {
      return ResponseEntity.status(403).build();
    }
    userService.blockUser(id, adminUser.get());
    User updatedUser = userService.getUserById(id);
    return ResponseEntity.ok(UserResponseDto.from(updatedUser));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a user by id", description = "Returns the user identified by id.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "User returned successfully",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = UserResponseDto.class))),
    @ApiResponse(
        responseCode = "404",
        description = "User not found",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ExceptionResponseDto.class)))
  })
  public ResponseEntity<UserResponseDto> getUserById(
      @Parameter(description = "Identifier of the user", example = "1") @PathVariable Long id) {
    User user = userService.getUserById(id);
    return ResponseEntity.ok(UserResponseDto.from(user));
  }

  private int sanitizePage(int page) {
    return Math.max(page, 0);
  }

  private int sanitizeSize(int size) {
    return size <= 0 ? DEFAULT_PAGE_SIZE : size;
  }

  private record UserUpdateRequest(
      @Schema(description = "New username", example = "new-user") String username,
      @Schema(description = "New email address", example = "new-user@example.com") String email,
      @Schema(description = "New raw password", example = "secret123") String password) {}

  private record UpdateProfileRequest(
      @Schema(description = "Editable profile fields") UserUpdateRequest request,
      @Schema(description = "Profile image file", type = "string", format = "binary")
          MultipartFile imageFile) {}
}
