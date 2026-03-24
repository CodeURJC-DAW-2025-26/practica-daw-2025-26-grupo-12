package es.codeurjc.grupo12.scissors_please.controller.api.v1.matches;

import es.codeurjc.grupo12.scissors_please.dto.MatchmakingStatusDto;
import es.codeurjc.grupo12.scissors_please.dto.matches.MatchBattleDto;
import es.codeurjc.grupo12.scissors_please.dto.matches.MatchDto;
import es.codeurjc.grupo12.scissors_please.dto.matches.MatchPageDto;
import es.codeurjc.grupo12.scissors_please.dto.matches.MatchStartResultDto;
import es.codeurjc.grupo12.scissors_please.dto.matches.MatchStatsDto;
import es.codeurjc.grupo12.scissors_please.dto.matches.RecentMatchesDto;
import es.codeurjc.grupo12.scissors_please.model.Match;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.service.match.MatchService;
import es.codeurjc.grupo12.scissors_please.service.user.UserService;
import es.codeurjc.grupo12.scissors_please.views.MatchListItem;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("apiMatchController")
@RequestMapping("/api/v1/matches")
@Tag(
    name = "Matches",
    description = "Operations for public match browsing and authenticated match workflows")
public class MatchController {

  @Autowired private MatchService matchService;
  @Autowired private UserService userService;

  @GetMapping("/{id}")
  @Operation(
      summary = "Get a match by id",
      description = "Returns the full match representation for the given match identifier.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Match found",
        content = @Content(schema = @Schema(implementation = MatchDto.class))),
    @ApiResponse(responseCode = "404", description = "Match not found", content = @Content)
  })
  public ResponseEntity<MatchDto> getMatch(
      @Parameter(description = "Match identifier", example = "1") @PathVariable Long id) {
    Match match = matchService.getMatchById(id).orElseThrow(NoSuchElementException::new);

    return ResponseEntity.ok(MatchDto.from(match));
  }

  @GetMapping
  @Operation(
      summary = "List best matches",
      description =
          "Returns a paginated list of the best matches ordered by the application scoring rules.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Page of matches returned",
        content = @Content(schema = @Schema(implementation = MatchPageDto.class)))
  })
  public ResponseEntity<MatchPageDto> getMatches(
      @Parameter(description = "Zero-based page index", example = "0")
          @RequestParam(value = "page", defaultValue = "0")
          int page,
      @Parameter(description = "Page size, capped to 20", example = "10")
          @RequestParam(value = "size", defaultValue = "10")
          int size) {

    int safePage = Math.max(page, 0);
    int safeSize = Math.min(Math.max(size, 1), 20);

    PageRequest pageable = PageRequest.of(safePage, safeSize);

    Page<MatchListItem> matchPage = matchService.getBestMatchPage(pageable);

    return ResponseEntity.ok(MatchPageDto.fromPage(matchPage));
  }

  @GetMapping("/{id}/stats")
  @Operation(
      summary = "Get match stats",
      description =
          "Returns the stats view for the match and, when authenticated, acknowledges the ready state for the current user.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Match stats returned",
        content = @Content(schema = @Schema(implementation = MatchStatsDto.class))),
    @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
    @ApiResponse(responseCode = "404", description = "Match not found", content = @Content)
  })
  public ResponseEntity<MatchStatsDto> getMatchStats(
      @Parameter(description = "Match identifier", example = "1") @PathVariable Long id,
      @Parameter(hidden = true) Authentication authentication) {
    Long currentUserId =
        authentication != null ? userService.getCurrentUser(authentication).getId() : null;
    MatchStatsDto matchStats = matchService.getMatchStatsView(id, currentUserId);
    if (currentUserId != null) matchService.acknowledgeReadyMatch(currentUserId, id);
    return ResponseEntity.ok(matchStats);
  }

  @GetMapping("/{id}/battle")
  @Operation(
      summary = "Get match battle view",
      description =
          "Returns the battle view for the given match and acknowledges readiness for authenticated users.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Battle view returned",
        content = @Content(schema = @Schema(implementation = MatchBattleDto.class))),
    @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
    @ApiResponse(responseCode = "404", description = "Match not found", content = @Content)
  })
  public ResponseEntity<MatchBattleDto> getMatchBattle(
      @Parameter(description = "Match identifier", example = "1") @PathVariable Long id,
      @Parameter(hidden = true) Authentication authentication) {
    MatchBattleDto battleView = matchService.getMatchBattleView(id);
    if (authentication != null) {
      Long userId = userService.getCurrentUser(authentication).getId();
      matchService.acknowledgeReadyMatch(userId, id);
    }
    return ResponseEntity.ok(battleView);
  }

  @GetMapping("/recent")
  @Operation(
      summary = "Get recent matches",
      description = "Returns the current user's recent match section.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Recent matches returned",
        content = @Content(schema = @Schema(implementation = RecentMatchesDto.class))),
    @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content)
  })
  public ResponseEntity<RecentMatchesDto> getRecentMatches(
      @Parameter(hidden = true) Authentication authentication) {
    Long userId = userService.getCurrentUser(authentication).getId();
    RecentMatchesDto section = matchService.getUserRecentMatchSection(userId);
    return ResponseEntity.ok(section);
  }

  @GetMapping("/matchmaking/status")
  @Operation(
      summary = "Get matchmaking status",
      description = "Returns the current user's matchmaking status.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Matchmaking status returned",
        content = @Content(schema = @Schema(implementation = MatchmakingStatusDto.class))),
    @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content)
  })
  public ResponseEntity<MatchmakingStatusDto> getMatchmakingStatus(
      @Parameter(hidden = true) Authentication authentication) {
    Long userId = userService.getCurrentUser(authentication).getId();
    MatchmakingStatusDto status = matchService.getMatchmakingStatus(userId);
    return ResponseEntity.ok(status);
  }

  @PostMapping("/matchmaking/start")
  @Operation(
      summary = "Start matchmaking",
      description =
          "Starts a matchmaking session for the current user, optionally using a bot identifier.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Matchmaking started",
        content = @Content(schema = @Schema(implementation = MatchStartResultDto.class))),
    @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content)
  })
  public ResponseEntity<MatchStartResultDto> startMatchmaking(
      @Parameter(description = "Optional bot identifier to use in matchmaking", example = "5")
          @RequestParam(name = "botId", required = false)
          Long botId,
      @Parameter(hidden = true) Authentication authentication) {

    User currentUser = userService.getCurrentUser(authentication);
    MatchStartResultDto result =
        matchService.startMatchmaking(currentUser.getId(), currentUser.getUsername(), botId);
    return ResponseEntity.ok(result);
  }

  @PostMapping("/matchmaking/cancel")
  @Operation(
      summary = "Cancel matchmaking",
      description = "Cancels the current user's active matchmaking session.")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Matchmaking cancelled", content = @Content),
    @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content)
  })
  public ResponseEntity<Void> cancelMatchmaking(
      @Parameter(hidden = true) Authentication authentication) {
    Long userId = userService.getCurrentUser(authentication).getId();
    matchService.cancelMatchmaking(userId);
    return ResponseEntity.ok().build();
  }

  @PostMapping("/{id}/rematch/request")
  @Operation(
      summary = "Request a rematch",
      description = "Creates a rematch invitation for the given match and current user.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Rematch invitation created",
        content = @Content(schema = @Schema(type = "string"))),
    @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
    @ApiResponse(responseCode = "404", description = "Match not found", content = @Content)
  })
  public ResponseEntity<String> requestRematch(
      @Parameter(description = "Match identifier", example = "1") @PathVariable Long id,
      @Parameter(hidden = true) Authentication authentication) {
    User currentUser = userService.getCurrentUser(authentication);
    String invitationId = matchService.requestRematch(id, currentUser);
    return ResponseEntity.ok(invitationId);
  }

  @PostMapping("/rematch/{invitationId}/accept")
  @Operation(
      summary = "Accept a rematch",
      description =
          "Accepts a rematch invitation and returns the redirect URL to continue the rematch flow.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Rematch accepted",
        content = @Content(schema = @Schema(type = "string"))),
    @ApiResponse(responseCode = "401", description = "Authentication required", content = @Content),
    @ApiResponse(responseCode = "404", description = "Invitation not found", content = @Content)
  })
  public ResponseEntity<String> acceptRematch(
      @Parameter(description = "Rematch invitation identifier", example = "abc123") @PathVariable
          String invitationId,
      @Parameter(hidden = true) Authentication authentication) {
    User currentUser = userService.getCurrentUser(authentication);
    String redirectUrl = matchService.acceptRematch(invitationId, currentUser);
    return ResponseEntity.ok(redirectUrl);
  }
}
