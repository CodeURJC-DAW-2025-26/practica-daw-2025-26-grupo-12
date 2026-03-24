package es.codeurjc.grupo12.scissors_please.controller.api.v1.tournaments;

import es.codeurjc.grupo12.scissors_please.dto.tournaments.TournamentCreateRequestDto;
import es.codeurjc.grupo12.scissors_please.dto.tournaments.TournamentDto;
import es.codeurjc.grupo12.scissors_please.dto.tournaments.TournamentJoinRequestDto;
import es.codeurjc.grupo12.scissors_please.dto.tournaments.TournamentJoinResultDto;
import es.codeurjc.grupo12.scissors_please.dto.tournaments.TournamentPageDto;
import es.codeurjc.grupo12.scissors_please.dto.tournaments.TournamentRequestDto;
import es.codeurjc.grupo12.scissors_please.model.Image;
import es.codeurjc.grupo12.scissors_please.model.Tournament;
import es.codeurjc.grupo12.scissors_please.service.image.ImageService;
import es.codeurjc.grupo12.scissors_please.service.tournament.TournamentService;
import es.codeurjc.grupo12.scissors_please.service.user.UserService;
import es.codeurjc.grupo12.scissors_please.views.JoinTournamentResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Tournaments", description = "Tournament management endpoints")
@RestController("apiTournamentController")
@RequestMapping("/api/v1/tournaments")
public class TournamentController {

  private final TournamentService tournamentService;
  private final ImageService imageService;
  private final UserService userService;

  public TournamentController(
      TournamentService tournamentService, ImageService imageService, UserService userService) {
    this.tournamentService = tournamentService;
    this.imageService = imageService;
    this.userService = userService;
  }

  @Operation(
      summary = "Create a tournament",
      description = "Creates a new tournament and optionally uploads its image.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Tournament created",
        content = @Content(schema = @Schema(implementation = TournamentDto.class))),
    @ApiResponse(responseCode = "400", description = "Invalid multipart request"),
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Not allowed to create tournaments")
  })
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      required = true,
      content =
          @Content(
              mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
              schema = @Schema(type = "object")))
  @PostMapping
  public ResponseEntity<TournamentDto> createTournament(
      @Parameter(
              description = "Tournament data",
              required = true,
              content =
                  @Content(schema = @Schema(implementation = TournamentCreateRequestDto.class)))
          @RequestPart("request")
          TournamentCreateRequestDto request,
      @Parameter(
              description = "Optional tournament image",
              content =
                  @Content(
                      mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                      schema = @Schema(type = "string", format = "binary")))
          @RequestPart(value = "imageFile", required = false)
          MultipartFile imageFile)
      throws IOException {
    Image image = imageService.convertToImage(imageFile);
    Tournament tournament =
        tournamentService.createTournament(
            request.name(),
            image,
            request.description(),
            request.slots(),
            request.registrationStarts(),
            request.startDate(),
            request.price());
    return ResponseEntity.status(HttpStatus.CREATED).body(TournamentDto.from(tournament));
  }

  @Operation(
      summary = "Delete a tournament",
      description = "Deletes a tournament by its id and returns the deleted representation.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Tournament deleted",
        content = @Content(schema = @Schema(implementation = TournamentDto.class))),
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Not allowed to delete tournaments"),
    @ApiResponse(responseCode = "404", description = "Tournament not found")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<TournamentDto> deleteTournament(
      @Parameter(description = "Tournament id", required = true) @PathVariable Long id) {
    Optional<Tournament> tournament = tournamentService.getTournamentById(id);
    if (tournament.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    TournamentDto deletedTournament = TournamentDto.from(tournament.get());
    tournamentService.deleteTournament(id);
    return ResponseEntity.ok(deletedTournament);
  }

  @Operation(
      summary = "Update a tournament",
      description = "Updates tournament fields and optionally replaces its image.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Tournament updated",
        content = @Content(schema = @Schema(implementation = TournamentDto.class))),
    @ApiResponse(responseCode = "400", description = "Invalid multipart request"),
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Not allowed to update tournaments"),
    @ApiResponse(responseCode = "404", description = "Tournament not found")
  })
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      required = true,
      content =
          @Content(
              mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
              schema = @Schema(type = "object")))
  @PutMapping("/{id}")
  public ResponseEntity<TournamentDto> updateTournament(
      @Parameter(description = "Tournament id", required = true) @PathVariable Long id,
      @Parameter(
              description = "Tournament data",
              required = true,
              content = @Content(schema = @Schema(implementation = TournamentRequestDto.class)))
          @RequestPart("request")
          TournamentRequestDto request,
      @Parameter(
              description = "Optional tournament image",
              content =
                  @Content(
                      mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                      schema = @Schema(type = "string", format = "binary")))
          @RequestPart(value = "imageFile", required = false)
          MultipartFile imageFile)
      throws IOException {
    Image image = imageService.convertToImage(imageFile);
    return tournamentService
        .updateTournament(
            id,
            request.name(),
            image,
            request.description(),
            request.status(),
            request.slots(),
            request.registrationStarts(),
            request.startDate(),
            request.price())
        .map(tournament -> ResponseEntity.ok(TournamentDto.from(tournament)))
        .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  @Operation(summary = "Get a tournament", description = "Returns a tournament by its id.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Tournament found",
        content = @Content(schema = @Schema(implementation = TournamentDto.class))),
    @ApiResponse(responseCode = "404", description = "Tournament not found")
  })
  @GetMapping("/{id}")
  public ResponseEntity<TournamentDto> getTournament(
      @Parameter(description = "Tournament id", required = true) @PathVariable Long id) {
    return tournamentService
        .getTournamentById(id)
        .map(t -> ResponseEntity.ok(TournamentDto.from(t)))
        .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  @Operation(
      summary = "List tournaments",
      description = "Returns a paginated list of tournaments, optionally filtered by query.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Page of tournaments",
        content = @Content(schema = @Schema(implementation = TournamentPageDto.class)))
  })
  @GetMapping
  public ResponseEntity<TournamentPageDto> getTournamentPage(
      @Parameter(description = "Optional search query")
          @RequestParam(value = "query", required = false)
          String query,
      @Parameter(description = "Zero-based page index", example = "0")
          @RequestParam(value = "page", defaultValue = "0")
          int page,
      @Parameter(description = "Page size", example = "10")
          @RequestParam(value = "size", defaultValue = "10")
          int size) {
    PageRequest pageable = PageRequest.of(page, size);
    TournamentPageDto tournamentPage =
        TournamentPageDto.fromPage(tournamentService.getTournamentPage(query, pageable));
    return ResponseEntity.ok(tournamentPage);
  }

  @Operation(
      summary = "Join a tournament",
      description = "Registers the current user and optionally a bot in a tournament.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Joined successfully",
        content = @Content(schema = @Schema(implementation = TournamentJoinResultDto.class))),
    @ApiResponse(responseCode = "400", description = "Invalid request payload"),
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "User or bot not allowed"),
    @ApiResponse(responseCode = "404", description = "Tournament not found"),
    @ApiResponse(responseCode = "409", description = "Tournament cannot accept the join request")
  })
  @PostMapping("/join")
  public ResponseEntity<TournamentJoinResultDto> joinTournament(
      @Parameter(
              description = "Join request payload",
              required = true,
              content = @Content(schema = @Schema(implementation = TournamentJoinRequestDto.class)))
          @RequestBody
          TournamentJoinRequestDto request,
      @Parameter(hidden = true) Authentication authentication) {
    if (request == null || request.tournamentId() == null) {
      return ResponseEntity.badRequest().build();
    }
    JoinTournamentResult result =
        tournamentService.joinTournament(
            request.tournamentId(), request.botId(), userService.getCurrentUser(authentication));
    TournamentJoinResultDto dto = TournamentJoinResultDto.from(result);

    return switch (result.status()) {
      case JOINED -> ResponseEntity.ok(dto);
      case TOURNAMENT_NOT_FOUND -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(dto);
      case INVALID_USER, ADMIN_NOT_ALLOWED -> ResponseEntity.status(HttpStatus.FORBIDDEN).body(dto);
      case INVALID_BOT -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto);
      case ALREADY_REGISTERED,
          BOT_ALREADY_REGISTERED,
          REGISTRATION_CLOSED,
          REGISTRATION_NOT_OPEN,
          TOURNAMENT_FULL ->
          ResponseEntity.status(HttpStatus.CONFLICT).body(dto);
    };
  }

  @Operation(
      summary = "List my tournaments",
      description = "Returns the tournaments associated with the authenticated user.")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Page of user tournaments",
        content = @Content(schema = @Schema(implementation = TournamentPageDto.class))),
    @ApiResponse(responseCode = "401", description = "Authentication required")
  })
  @GetMapping("/my-tournaments")
  public ResponseEntity<TournamentPageDto> getMyTournaments(
      @Parameter(description = "Optional search query")
          @RequestParam(value = "query", required = false)
          String query,
      @Parameter(description = "Zero-based page index", example = "0")
          @RequestParam(value = "page", defaultValue = "0")
          int page,
      @Parameter(description = "Page size", example = "10")
          @RequestParam(value = "size", defaultValue = "10")
          int size,
      @Parameter(hidden = true) Authentication authentication) {
    Long currentUserId = userService.getCurrentUser(authentication).getId();
    PageRequest pageable = PageRequest.of(page, size);
    TournamentPageDto tournamentPage =
        TournamentPageDto.fromPage(
            tournamentService.getUserTournamentPage(currentUserId, query, pageable));
    return ResponseEntity.ok(tournamentPage);
  }
}
