package es.codeurjc.grupo12.scissors_please.controller.api.v1.bots;

import es.codeurjc.grupo12.scissors_please.config.ResponseConstants;
import es.codeurjc.grupo12.scissors_please.dto.ResponseDto;
import es.codeurjc.grupo12.scissors_please.dto.bots.BotCreateRequestDTO;
import es.codeurjc.grupo12.scissors_please.dto.bots.BotDTO;
import es.codeurjc.grupo12.scissors_please.dto.bots.BotDTOWithSimpleImage;
import es.codeurjc.grupo12.scissors_please.dto.bots.BotPageResponseDTO;
import es.codeurjc.grupo12.scissors_please.dto.bots.BotUpdateRequestDTO;
import es.codeurjc.grupo12.scissors_please.exception.BotAccessDeniedException;
import es.codeurjc.grupo12.scissors_please.exception.BotImageUploadException;
import es.codeurjc.grupo12.scissors_please.exception.BotNotFoundException;
import es.codeurjc.grupo12.scissors_please.model.Bot;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.service.bot.BotService;
import es.codeurjc.grupo12.scissors_please.service.user.UserService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
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
public class BotController {

  private static final int DEFAULT_PAGE_SIZE = 10;
  private static final int MAX_PAGE_SIZE = 20;

  @Autowired private BotService botService;
  @Autowired private UserService userService;

  @GetMapping("/{id}")
  public ResponseDto getBot(@PathVariable Long id, Authentication authentication) {
    try {
      Bot bot = botService.getUserBot(resolveCurrentUser(authentication), id);
      return new ResponseDto(
          false, ResponseConstants.OK_CODE_INT, ResponseConstants.OK, toBotDto(bot));
    } catch (BotNotFoundException exception) {
      return new ResponseDto(
          true, ResponseConstants.NOT_FOUND_CODE_INT, ResponseConstants.BOT_NOT_FOUND, null);
    } catch (BotAccessDeniedException exception) {
      return new ResponseDto(
          true, ResponseConstants.FORBIDDEN_CODE_INT, ResponseConstants.ACCESS_DENIED, null);
    }
  }

  @GetMapping("/user/{userId}")
  public ResponseDto getUserBots(
      @PathVariable Long userId,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size,
      Authentication authentication) {
    try {
      PageRequest pageable = PageRequest.of(sanitizePage(page), sanitizeSize(size));
      Optional<Long> requesterId = resolveCurrentUser(authentication).map(User::getId);
      Page<BotDTOWithSimpleImage> botPage =
          botService.getUserBots(requesterId, userId, pageable).map(this::toBotSummaryDto);

      return new ResponseDto(
          false,
          ResponseConstants.OK_CODE_INT,
          ResponseConstants.OK,
          toBotPageResponseDto(botPage));
    } catch (IllegalArgumentException exception) {
      return new ResponseDto(
          true, ResponseConstants.NOT_FOUND_CODE_INT, ResponseConstants.ELEMENT_NOT_FOUND, null);
    }
  }

  @PostMapping
  public ResponseDto createBot(
      @ModelAttribute BotCreateRequestDTO request, Authentication authentication) {
    if (!isAuthenticated(authentication)) {
      return new ResponseDto(
          true, ResponseConstants.UNAUTHORIZED_CODE_INT, ResponseConstants.ACCESS_DENIED, null);
    }

    User currentUser = userService.getCurrentUser(authentication);
    if (userService.isAdmin(currentUser)) {
      return new ResponseDto(
          true, ResponseConstants.FORBIDDEN_CODE_INT, ResponseConstants.ACCESS_DENIED, null);
    }

    try {
      Bot bot =
          botService.createBot(
              currentUser,
              request.getName(),
              request.getDescription(),
              request.getTags(),
              request.getImageFile(),
              request.isPublic());
      return new ResponseDto(
          false, ResponseConstants.OK_CODE_INT, ResponseConstants.OK, toBotDto(bot));
    } catch (BotAccessDeniedException exception) {
      return new ResponseDto(
          true, ResponseConstants.FORBIDDEN_CODE_INT, ResponseConstants.ACCESS_DENIED, null);
    } catch (BotImageUploadException exception) {
      return new ResponseDto(
          true, ResponseConstants.BAD_REQUEST_CODE_INT, ResponseConstants.IMAGE_ERROR_UPLOAD, null);
    }
  }

  @PutMapping("/{id}")
  public ResponseDto updateBot(
      @PathVariable Long id,
      @ModelAttribute BotUpdateRequestDTO request,
      Authentication authentication) {
    if (!isAuthenticated(authentication)) {
      return new ResponseDto(
          true, ResponseConstants.UNAUTHORIZED_CODE_INT, ResponseConstants.ACCESS_DENIED, null);
    }

    User currentUser = userService.getCurrentUser(authentication);

    try {
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

      return new ResponseDto(
          false, ResponseConstants.OK_CODE_INT, ResponseConstants.OK, toBotDto(updatedBot));
    } catch (BotNotFoundException exception) {
      return new ResponseDto(
          true, ResponseConstants.NOT_FOUND_CODE_INT, ResponseConstants.BOT_NOT_FOUND, null);
    } catch (BotAccessDeniedException exception) {
      return new ResponseDto(
          true, ResponseConstants.FORBIDDEN_CODE_INT, ResponseConstants.ACCESS_DENIED, null);
    } catch (BotImageUploadException exception) {
      return new ResponseDto(
          true, ResponseConstants.BAD_REQUEST_CODE_INT, ResponseConstants.IMAGE_ERROR_UPLOAD, null);
    }
  }

  @DeleteMapping("/{id}")
  public ResponseDto deleteBot(@PathVariable Long id, Authentication authentication) {
    if (!isAuthenticated(authentication)) {
      return new ResponseDto(
          true, ResponseConstants.UNAUTHORIZED_CODE_INT, ResponseConstants.ACCESS_DENIED, null);
    }

    User currentUser = userService.getCurrentUser(authentication);

    try {
      Bot botToDelete = botService.getEditableBotOrThrow(id, currentUser);
      botService.deleteBot(currentUser, id);
      return new ResponseDto(
          false, ResponseConstants.OK_CODE_INT, ResponseConstants.OK, toBotSummaryDto(botToDelete));
    } catch (BotNotFoundException exception) {
      return new ResponseDto(
          true, ResponseConstants.NOT_FOUND_CODE_INT, ResponseConstants.BOT_NOT_FOUND, null);
    } catch (BotAccessDeniedException exception) {
      return new ResponseDto(
          true, ResponseConstants.FORBIDDEN_CODE_INT, ResponseConstants.ACCESS_DENIED, null);
    }
  }

  private Optional<User> resolveCurrentUser(Authentication authentication) {
    if (!isAuthenticated(authentication)) {
      return Optional.empty();
    }
    return Optional.of(userService.getCurrentUser(authentication));
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
    return "/bot-images/" + bot.getId();
  }
}
