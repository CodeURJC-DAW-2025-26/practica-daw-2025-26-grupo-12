package es.codeurjc.grupo12.scissors_please.controller.api.v1.user;

import es.codeurjc.grupo12.scissors_please.dto.ExceptionResponseDto;
import es.codeurjc.grupo12.scissors_please.dto.users.UpdateProfileRequestDto;
import es.codeurjc.grupo12.scissors_please.dto.users.UserPageResponseDto;
import es.codeurjc.grupo12.scissors_please.dto.users.UserResponseDto;
import es.codeurjc.grupo12.scissors_please.dto.users.UserUpdateRequestDto;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
  private static final Logger log = LoggerFactory.getLogger(UserController.class);

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

  @GetMapping("/me")
  @Operation(
      summary = "Get current authenticated user",
      description = "Returns the profile of the current authenticated user.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Current user returned",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = UserResponseDto.class))),
    @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content)
  })
  public ResponseEntity<UserResponseDto> getMe(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(401).build();
    }
    try {
      User currentUser = userService.getCurrentUser(authentication);
      return ResponseEntity.ok(UserResponseDto.from(currentUser));
    } catch (Exception e) {
      return ResponseEntity.status(401).build();
    }
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
                      schema = @Schema(implementation = UpdateProfileRequestDto.class))))
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
      @Parameter(description = "Identifier of the user to update", example = "1") @PathVariable
          Long id,
      @Parameter(
              name = "request",
              description = "Multipart part with the editable profile fields serialized as JSON",
              required = true,
              content = @Content(schema = @Schema(implementation = UserUpdateRequestDto.class)))
          @RequestPart("request")
          UserUpdateRequestDto request,
      @Parameter(
              name = "imageFile",
              description = "New profile image",
              required = false,
              content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE))
          @RequestPart(value = "imageFile", required = false)
          MultipartFile imageFile,
      @Parameter(hidden = true) Authentication authentication)
      throws IOException {
    log.debug("updateProfile[{}] - start", id);
    log.debug(
        "updateProfile[{}] - auth present={}, authenticated={}, principal={}",
        id,
        authentication != null,
        authentication != null && authentication.isAuthenticated(),
        authentication != null ? authentication.getName() : null);
    log.debug(
        "updateProfile[{}] - request present={}, username={}, email={}, passwordPresent={}",
        id,
        request != null,
        request != null ? request.username() : null,
        request != null ? request.email() : null,
        request != null && request.password() != null && !request.password().isBlank());
    log.debug(
        "updateProfile[{}] - image present={}, empty={}, contentType={}, filename={}",
        id,
        imageFile != null,
        imageFile != null && imageFile.isEmpty(),
        imageFile != null ? imageFile.getContentType() : null,
        imageFile != null ? imageFile.getOriginalFilename() : null);

    if (authentication == null || !authentication.isAuthenticated()) {
      log.debug("updateProfile[{}] - abort unauthenticated", id);
      return ResponseEntity.status(401).build();
    }

    log.debug("updateProfile[{}] - loading user", id);
    User userToUpdate = userService.getUserById(id);
    log.debug(
        "updateProfile[{}] - loaded user username={}, email={}, hasImage={}",
        id,
        userToUpdate.getUsername(),
        userToUpdate.getEmail(),
        userToUpdate.getImage() != null);

    log.debug("updateProfile[{}] - converting image", id);
    Image image = imageService.convertToImage(imageFile);
    log.debug("updateProfile[{}] - image converted={}", id, image != null);

    if (request.username() != null && !request.username().isBlank()) {
      log.debug(
          "updateProfile[{}] - updating username from {} to {}",
          id,
          userToUpdate.getUsername(),
          request.username());
      userToUpdate.setUsername(request.username());
    } else {
      log.debug("updateProfile[{}] - username unchanged", id);
    }
    if (request.email() != null && !request.email().isBlank()) {
      log.debug(
          "updateProfile[{}] - updating email from {} to {}",
          id,
          userToUpdate.getEmail(),
          request.email());
      userToUpdate.setEmail(request.email());
    } else {
      log.debug("updateProfile[{}] - email unchanged", id);
    }
    if (request.password() != null && !request.password().isBlank()) {
      log.debug("updateProfile[{}] - updating password", id);
      userToUpdate.setPassword(passwordEncoder.encode(request.password()));
    } else {
      log.debug("updateProfile[{}] - password unchanged", id);
    }
    if (image != null) {
      log.debug("updateProfile[{}] - assigning new image to user", id);
      userToUpdate.setImage(image);
    } else {
      log.debug("updateProfile[{}] - image unchanged", id);
    }

    log.debug("updateProfile[{}] - persisting user", id);
    userService.updateUser(userToUpdate);
    log.debug("updateProfile[{}] - completed", id);

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
      @Parameter(description = "Identifier of the user to block", example = "1") @PathVariable
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

  @PutMapping("/{id}/unblock")
  @Operation(
      summary = "Unblock a user",
      description = "Unblocks the user identified by id using the authenticated admin user.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "User unblocked successfully",
        content =
            @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = UserResponseDto.class))),
    @ApiResponse(
        responseCode = "403",
        description = "Current user is not allowed to unblock users",
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
  public ResponseEntity<UserResponseDto> unblockUser(
      @Parameter(description = "Identifier of the user to unblock", example = "1") @PathVariable
          Long id,
      @Parameter(hidden = true) Authentication authentication) {
    String username = authentication.getName();
    Optional<User> adminUser = userService.findByUsername(username);
    if (adminUser.isEmpty() || !userService.isAdmin(adminUser.get())) {
      return ResponseEntity.status(403).build();
    }
    userService.unblockUser(id, adminUser.get());
    User updatedUser = userService.getUserById(id);
    return ResponseEntity.ok(UserResponseDto.from(updatedUser));
  }

  @org.springframework.web.bind.annotation.DeleteMapping("/{id}")
  @Operation(
      summary = "Delete a user",
      description = "Deletes the user identified by id using the authenticated admin user.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "User deleted successfully",
        content = @Content),
    @ApiResponse(
        responseCode = "403",
        description = "Current user is not allowed to delete users",
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
  public ResponseEntity<Void> deleteUser(
      @Parameter(description = "Identifier of the user to delete", example = "1") @PathVariable
          Long id,
      @Parameter(hidden = true) Authentication authentication) {
    String username = authentication.getName();
    Optional<User> adminUser = userService.findByUsername(username);
    if (adminUser.isEmpty() || !userService.isAdmin(adminUser.get())) {
      return ResponseEntity.status(403).build();
    }
    userService.deleteUser(id, adminUser.get());
    return ResponseEntity.ok().build();
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
}
