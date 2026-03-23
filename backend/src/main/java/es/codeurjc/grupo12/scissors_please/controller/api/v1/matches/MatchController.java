package es.codeurjc.grupo12.scissors_please.controller.api.v1.matches;

import es.codeurjc.grupo12.scissors_please.dto.MatchBattleDto;
import es.codeurjc.grupo12.scissors_please.dto.MatchDto;
import es.codeurjc.grupo12.scissors_please.dto.MatchPageDto;
import es.codeurjc.grupo12.scissors_please.dto.MatchStartResultDto;
import es.codeurjc.grupo12.scissors_please.dto.MatchStatsDto;
import es.codeurjc.grupo12.scissors_please.dto.MatchmakingStatusDto;
import es.codeurjc.grupo12.scissors_please.dto.RecentMatchesDto;
import es.codeurjc.grupo12.scissors_please.model.Match;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.service.match.MatchService;
import es.codeurjc.grupo12.scissors_please.service.user.UserService;
import es.codeurjc.grupo12.scissors_please.views.MatchListItem;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController("apiMatchController")
@RequestMapping("/api/v1/matches")
public class MatchController {

  @Autowired private MatchService matchService;
  @Autowired private UserService userService;

  @GetMapping("/{id}")
  public ResponseEntity<MatchDto> getMatch(@PathVariable Long id) {
    Match match = matchService.getMatchById(id).orElseThrow(NoSuchElementException::new);

    return ResponseEntity.ok(MatchDto.from(match));
  }

  @GetMapping
  public ResponseEntity<MatchPageDto> getMatches(
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size) {

    int safePage = Math.max(page, 0);
    int safeSize = Math.min(Math.max(size, 1), 20);

    PageRequest pageable = PageRequest.of(safePage, safeSize);

    Page<MatchListItem> matchPage = matchService.getBestMatchPage(pageable);

    return ResponseEntity.ok(MatchPageDto.fromPage(matchPage));
  }

  @GetMapping("/{id}/stats")
  public ResponseEntity<MatchStatsDto> getMatchStats(
      @PathVariable Long id, Authentication authentication) {
    Long currentUserId =
        authentication != null ? userService.getCurrentUser(authentication).getId() : null;
    MatchStatsDto matchStats = matchService.getMatchStatsView(id, currentUserId);
    if (currentUserId != null) matchService.acknowledgeReadyMatch(currentUserId, id);
    return ResponseEntity.ok(matchStats);
  }

  @GetMapping("/{id}/battle")
  public ResponseEntity<MatchBattleDto> getMatchBattle(
      @PathVariable Long id, Authentication authentication) {
    MatchBattleDto battleView = matchService.getMatchBattleView(id);
    if (authentication != null) {
      Long userId = userService.getCurrentUser(authentication).getId();
      matchService.acknowledgeReadyMatch(userId, id);
    }
    return ResponseEntity.ok(battleView);
  }

  @GetMapping("/recent")
  public ResponseEntity<RecentMatchesDto> getRecentMatches(Authentication authentication) {
    Long userId = userService.getCurrentUser(authentication).getId();
    RecentMatchesDto section = matchService.getUserRecentMatchSection(userId);
    return ResponseEntity.ok(section);
  }

  @GetMapping("/matchmaking/status")
  public ResponseEntity<MatchmakingStatusDto> getMatchmakingStatus(Authentication authentication) {
    Long userId = userService.getCurrentUser(authentication).getId();
    MatchmakingStatusDto status = matchService.getMatchmakingStatus(userId);
    return ResponseEntity.ok(status);
  }

  @PostMapping("/matchmaking/start")
  public ResponseEntity<MatchStartResultDto> startMatchmaking(
      @RequestParam(name = "botId", required = false) Long botId, Authentication authentication) {

    User currentUser = userService.getCurrentUser(authentication);
    MatchStartResultDto result =
        matchService.startMatchmaking(currentUser.getId(), currentUser.getUsername(), botId);
    return ResponseEntity.ok(result);
  }

  @PostMapping("/matchmaking/cancel")
  public ResponseEntity<Void> cancelMatchmaking(Authentication authentication) {
    Long userId = userService.getCurrentUser(authentication).getId();
    matchService.cancelMatchmaking(userId);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{id}/rematch/request")
  public ResponseEntity<String> requestRematch(
      @PathVariable Long id, Authentication authentication) {
    User currentUser = userService.getCurrentUser(authentication);
    String invitationId = matchService.requestRematch(id, currentUser);
    return ResponseEntity.ok(invitationId);
  }

  @PostMapping("/rematch/{invitationId}/accept")
  public ResponseEntity<String> acceptRematch(
      @PathVariable String invitationId, Authentication authentication) {
    User currentUser = userService.getCurrentUser(authentication);
    String redirectUrl = matchService.acceptRematch(invitationId, currentUser);
    return ResponseEntity.ok(redirectUrl);
  }
}
