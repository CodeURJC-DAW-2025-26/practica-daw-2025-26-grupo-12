package es.codeurjc.grupo12.scissors_please.controller.api.v1.bots;

import es.codeurjc.grupo12.scissors_please.config.ResponseConstants;
import es.codeurjc.grupo12.scissors_please.dto.bots.BotCreateRequestDTO;
import es.codeurjc.grupo12.scissors_please.dto.bots.BotDTO;
import es.codeurjc.grupo12.scissors_please.dto.bots.BotDTOWithSimpleImage;
import es.codeurjc.grupo12.scissors_please.dto.bots.BotPageResponseDTO;
import es.codeurjc.grupo12.scissors_please.dto.bots.BotUpdateRequestDTO;
import es.codeurjc.grupo12.scissors_please.exception.BotAccessDeniedException;
import es.codeurjc.grupo12.scissors_please.model.Bot;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.service.bot.BotService;
import es.codeurjc.grupo12.scissors_please.service.user.UserService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.http.MediaType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("apiBotController")
@RequestMapping("/api/v1/bots")
@Tag(name = "Bots", description = "Operations for listing, creating, updating and deleting bots")
public class BotController {

  private static final int DEFAULT_PAGE_SIZE = 10;
  private static final int MAX_PAGE_SIZE = 20;

  @Autowired private BotService botService;
  @Autowired private UserService userService;

  @GetMapping
  @Operation(
      summary = "List bots",
      description = "Returns a paginated list of bots filtered by query and contextualized by the authenticated user.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Bots returned successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = BotPageResponseDTO.class)))
      })
  public ResponseEntity<BotPageResponseDTO> getBots(
      @RequestParam(value = "query", required = false) String query,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size,
      @Parameter(hidden = true) Authentication authentication) {
    PageRequest pageable = PageRequest.of(sanitizePage(page), sanitizeSize(size));
    Page<BotDTOWithSimpleImage> botPage =
        botService
            .getBotPage(resolveCurrentUser(authentication), query, pageable)
            .map(this::toBotSummaryDto);

    return ResponseEntity.ok(toBotPageResponseDto(botPage));
  }

  @GetMapping("/{id}")
  @Operation(
      summary = "Get bot by id",
      description = "Returns the detailed data for a bot that the current user can access.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Bot returned successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = BotDTO.class))),
        @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
        @ApiResponse(responseCode = "404", description = "Bot not found", content = @Content)
      })
  public ResponseEntity<BotDTO> getBot(
      @PathVariable Long id, @Parameter(hidden = true) Authentication authentication) {
    Bot bot = botService.getUserBot(resolveCurrentUser(authentication), id);
    return ResponseEntity.ok(toBotDto(bot));
  }

  @GetMapping("/user/{userId}")
  @Operation(
      summary = "List bots by user",
      description = "Returns the public bots for the selected user, with extra context for the requester when authenticated.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Bots returned successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = BotPageResponseDTO.class)))
      })
  public ResponseEntity<BotPageResponseDTO> getUserBots(
      @PathVariable Long userId,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size,
      @Parameter(hidden = true) Authentication authentication) {
    PageRequest pageable = PageRequest.of(sanitizePage(page), sanitizeSize(size));
    Optional<Long> requesterId = resolveCurrentUser(authentication).map(User::getId);
    Page<BotDTOWithSimpleImage> botPage =
        botService.getUserBots(requesterId, userId, pageable).map(this::toBotSummaryDto);

    return ResponseEntity.ok(toBotPageResponseDto(botPage));
  }

  @PostMapping
  @Operation(
      summary = "Create bot",
      description = "Creates a new bot for the authenticated user using multipart/form-data.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Bot created successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = BotDTO.class))),
        @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
        @ApiResponse(responseCode = "403", description = "Access denied", content = @Content)
      })
  public ResponseEntity<BotDTO> createBot(
      @RequestBody(
              content =
                  @Content(
                      mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                      schema = @Schema(implementation = BotCreateRequestDTO.class)))
          @ModelAttribute
          BotCreateRequestDTO request,
      @Parameter(hidden = true) Authentication authentication) {
    User currentUser = requireCurrentUser(authentication);
    if (userService.isAdmin(currentUser)) {
      throw new BotAccessDeniedException(ResponseConstants.ACCESS_DENIED);
    }

    Bot bot =
        botService.createBot(
            currentUser,
            request.getName(),
            request.getDescription(),
            request.getTags(),
            request.getImageFile(),
            request.isPublic());
    return ResponseEntity.ok(toBotDto(bot));
  }

  @PutMapping("/{id}")
  @Operation(
      summary = "Update bot",
      description = "Updates an editable bot using multipart/form-data.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Bot updated successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = BotDTO.class))),
        @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
        @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
        @ApiResponse(responseCode = "404", description = "Bot not found", content = @Content)
      })
  public ResponseEntity<BotDTO> updateBot(
      @PathVariable Long id,
      @RequestBody(
              content =
                  @Content(
                      mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                      schema = @Schema(implementation = BotUpdateRequestDTO.class)))
          @ModelAttribute
          BotUpdateRequestDTO request,
      @Parameter(hidden = true) Authentication authentication) {
    User currentUser = requireCurrentUser(authentication);
    Bot updatedBot =
        botService.updateBot(
            currentUser,
            id,
            request.getName(),
            request.getDescription(),
            request.getCode(),
            request.getImageFile(),
            request.getTags(),
            request.isPublic());

    return ResponseEntity.ok(toBotDto(updatedBot));
  }

  @DeleteMapping("/{id}")
  @Operation(
      summary = "Delete bot",
      description = "Deletes an editable bot and returns the deleted bot summary.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Bot deleted successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = BotDTOWithSimpleImage.class))),
        @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
        @ApiResponse(responseCode = "403", description = "Access denied", content = @Content),
        @ApiResponse(responseCode = "404", description = "Bot not found", content = @Content)
      })
  public ResponseEntity<BotDTOWithSimpleImage> deleteBot(
      @PathVariable Long id, @Parameter(hidden = true) Authentication authentication) {
    User currentUser = requireCurrentUser(authentication);
    Bot botToDelete = botService.getEditableBotOrThrow(id, currentUser);
    botService.deleteBot(currentUser, id);
    return ResponseEntity.ok(toBotSummaryDto(botToDelete));
  }

  private Optional<User> resolveCurrentUser(Authentication authentication) {
    if (!isAuthenticated(authentication)) {
      return Optional.empty();
    }
    try {
      return Optional.of(userService.getCurrentUser(authentication));
    } catch (IllegalArgumentException exception) {
      return Optional.empty();
    }
  }

  private User requireCurrentUser(Authentication authentication) {
    if (!isAuthenticated(authentication)) {
      throw new InsufficientAuthenticationException(ResponseConstants.ACCESS_DENIED);
    }
    try {
      return userService.getCurrentUser(authentication);
    } catch (IllegalArgumentException exception) {
      throw new InsufficientAuthenticationException(ResponseConstants.ACCESS_DENIED);
    }
  }

  private boolean isAuthenticated(Authentication authentication) {
    return authentication != null
        && authentication.isAuthenticated()
        && !(authentication instanceof AnonymousAuthenticationToken);
  }

  private int sanitizePage(int page) {
    return Math.max(page, 0);
  }

  private int sanitizeSize(int size) {
    if (size <= 0) {
      return DEFAULT_PAGE_SIZE;
    }
    return Math.min(size, MAX_PAGE_SIZE);
  }

  private BotDTO toBotDto(Bot bot) {
    List<String> tags = bot.getTags() == null ? List.of() : List.copyOf(bot.getTags());
    List<Integer> eloHistory =
        bot.getEloHistory() == null ? List.of() : List.copyOf(bot.getEloHistory());

    return new BotDTO(
        bot.getId(),
        bot.getName(),
        bot.getDescription(),
        bot.getCode(),
        bot.isPublic(),
        bot.getElo(),
        bot.getOwnerId(),
        bot.getWins(),
        bot.getLosses(),
        bot.getDraws(),
        tags,
        eloHistory,
        bot.isHasImage(),
        buildImageUrl(bot),
        bot.getCreatedAt(),
        bot.getUpdatedAt());
  }

  private BotDTOWithSimpleImage toBotSummaryDto(Bot bot) {
    List<String> tags = bot.getTags() == null ? List.of() : List.copyOf(bot.getTags());

    return new BotDTOWithSimpleImage(
        bot.getId(),
        bot.getName(),
        bot.getDescription(),
        bot.isPublic(),
        bot.getElo(),
        bot.getOwnerId(),
        bot.getWins(),
        bot.getLosses(),
        bot.getDraws(),
        tags,
        bot.isHasImage(),
        buildImageUrl(bot),
        bot.getCreatedAt(),
        bot.getUpdatedAt());
  }

  private BotPageResponseDTO toBotPageResponseDto(Page<BotDTOWithSimpleImage> botPage) {
    return new BotPageResponseDTO(
        botPage.getContent(),
        botPage.getNumber(),
        botPage.getSize(),
        botPage.getTotalElements(),
        botPage.getTotalPages(),
        botPage.hasNext(),
        botPage.hasPrevious(),
        botPage.isFirst(),
        botPage.isLast());
  }

  private String buildImageUrl(Bot bot) {
    if (bot == null || bot.getId() == null || !bot.isHasImage()) {
      return null;
    }
    return "/api/v1/images/bots/" + bot.getId();
  }
}
