package es.codeurjc.grupo12.scissors_please.controller.api.v1.matches;

import es.codeurjc.grupo12.scissors_please.config.ResponseConstants;
import es.codeurjc.grupo12.scissors_please.dto.ResponseDto;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.service.match.MatchService;
import es.codeurjc.grupo12.scissors_please.service.user.UserService;
import es.codeurjc.grupo12.scissors_please.views.MatchStartResult;
import es.codeurjc.grupo12.scissors_please.views.MatchmakingStatusView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("apiMatchController")
@RequestMapping("/api/v1/matches")
public class MatchController {

  @Autowired MatchService matchService;
  @Autowired UserService userService;

  @GetMapping("/{id}")
  public ResponseDto getMatch(@PathVariable Long id) {
    return matchService
        .getMatchById(id)
        .map(
            match ->
                new ResponseDto(false, ResponseConstants.OK_CODE_INT, ResponseConstants.OK, match))
        .orElse(
            new ResponseDto(
                true,
                ResponseConstants.NOT_FOUND_CODE_INT,
                ResponseConstants.ELEMENT_NOT_FOUND,
                null));
  }

  @GetMapping
  public ResponseDto getMatches(
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size) {

    int safePage = Math.max(page, 0);
    int safeSize = Math.min(Math.max(size, 1), 20);

    PageRequest pageable = PageRequest.of(safePage, safeSize);

    var matchPage = matchService.getBestMatchPage(pageable);

    return new ResponseDto(false, ResponseConstants.OK_CODE_INT, ResponseConstants.OK, matchPage);
  }

  @GetMapping("/{id}/stats")
  public ResponseDto getMatchStats(@PathVariable Long id, Authentication authentication) {
    try {
      Long currentUserId =
          authentication != null ? userService.getCurrentUser(authentication).getId() : null;

      var matchStats = matchService.getMatchStatsView(id, currentUserId);

      if (currentUserId != null) {
        matchService.acknowledgeReadyMatch(currentUserId, id);
      }

      return new ResponseDto(
          false, ResponseConstants.OK_CODE_INT, ResponseConstants.OK, matchStats);
    } catch (IllegalArgumentException e) {
      return new ResponseDto(true, ResponseConstants.NOT_FOUND_CODE_INT, e.getMessage(), null);
    }
  }

  @GetMapping("/{id}/battle")
  public ResponseDto getMatchBattle(@PathVariable Long id, Authentication authentication) {
    try {
      var battleView = matchService.getMatchBattleView(id);

      if (authentication != null) {
        Long userId = userService.getCurrentUser(authentication).getId();
        matchService.acknowledgeReadyMatch(userId, id);
      }

      return new ResponseDto(
          false, ResponseConstants.OK_CODE_INT, ResponseConstants.OK, battleView);
    } catch (IllegalArgumentException e) {
      return new ResponseDto(true, ResponseConstants.NOT_FOUND_CODE_INT, e.getMessage(), null);
    }
  }

  @GetMapping("/recent")
  public ResponseDto getRecentMatches(Authentication authentication) {
    Long userId = userService.getCurrentUser(authentication).getId();
    var section = matchService.getUserRecentMatchSection(userId);

    return new ResponseDto(false, ResponseConstants.OK_CODE_INT, ResponseConstants.OK, section);
  }

  @GetMapping("/matchmaking/status")
  public ResponseDto getMatchmakingStatus(Authentication authentication) {
    Long userId = userService.getCurrentUser(authentication).getId();
    MatchmakingStatusView status = matchService.getMatchmakingStatus(userId);

    return new ResponseDto(false, ResponseConstants.OK_CODE_INT, ResponseConstants.OK, status);
  }

  @PostMapping("/matchmaking/start")
  public ResponseDto startMatchmaking(
      @RequestParam(name = "botId", required = false) Long botId, Authentication authentication) {
    User currentUser = userService.getCurrentUser(authentication);
    try {
      MatchStartResult result =
          matchService.startMatchmaking(currentUser.getId(), currentUser.getUsername(), botId);
      return new ResponseDto(false, ResponseConstants.OK_CODE_INT, ResponseConstants.OK, result);
    } catch (IllegalArgumentException e) {
      return new ResponseDto(true, ResponseConstants.BAD_REQUEST_CODE_INT, e.getMessage(), null);
    }
  }

  @PostMapping("/matchmaking/cancel")
  public ResponseDto cancelMatchmaking(Authentication authentication) {
    Long userId = userService.getCurrentUser(authentication).getId();
    try {
      matchService.cancelMatchmaking(userId);
      return new ResponseDto(false, ResponseConstants.OK_CODE_INT, ResponseConstants.OK, null);
    } catch (IllegalArgumentException e) {
      return new ResponseDto(true, ResponseConstants.CONFLICT_CODE_INT, e.getMessage(), null);
    }
  }

  @PostMapping("/{id}/rematch/request")
  public ResponseDto requestRematch(@PathVariable Long id, Authentication authentication) {
    User currentUser = userService.getCurrentUser(authentication);
    try {
      String invitationId = matchService.requestRematch(id, currentUser);
      return new ResponseDto(
          false, ResponseConstants.OK_CODE_INT, ResponseConstants.OK, invitationId);
    } catch (IllegalArgumentException e) {
      return new ResponseDto(true, ResponseConstants.BAD_REQUEST_CODE_INT, e.getMessage(), null);
    }
  }

  @PostMapping("/rematch/{invitationId}/accept")
  public ResponseDto acceptRematch(
      @PathVariable String invitationId, Authentication authentication) {
    User currentUser = userService.getCurrentUser(authentication);
    try {
      String redirectUrl = matchService.acceptRematch(invitationId, currentUser);
      return new ResponseDto(
          false, ResponseConstants.OK_CODE_INT, ResponseConstants.OK, redirectUrl);
    } catch (IllegalArgumentException e) {
      return new ResponseDto(true, ResponseConstants.BAD_REQUEST_CODE_INT, e.getMessage(), null);
    }
  }
}
