package es.codeurjc.grupo12.scissors_please.controller.api.v1.tournaments;

import es.codeurjc.grupo12.scissors_please.dto.TournamentDto;
import es.codeurjc.grupo12.scissors_please.dto.TournamentCreateRequestDto;
import es.codeurjc.grupo12.scissors_please.dto.TournamentJoinRequestDto;
import es.codeurjc.grupo12.scissors_please.dto.TournamentJoinResultDto;
import es.codeurjc.grupo12.scissors_please.dto.TournamentPageDto;
import es.codeurjc.grupo12.scissors_please.dto.TournamentRequestDto;
import es.codeurjc.grupo12.scissors_please.model.Image;
import es.codeurjc.grupo12.scissors_please.model.Tournament;
import es.codeurjc.grupo12.scissors_please.service.image.ImageService;
import es.codeurjc.grupo12.scissors_please.service.tournament.TournamentService;
import es.codeurjc.grupo12.scissors_please.service.user.UserService;
import es.codeurjc.grupo12.scissors_please.views.JoinTournamentResult;
import java.io.IOException;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
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

  @PostMapping
  public ResponseEntity<TournamentDto> createTournament(
      @RequestPart("request") TournamentCreateRequestDto request,
      @RequestPart(value = "imageFile", required = false) MultipartFile imageFile)
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

  @DeleteMapping("/{id}")
  public ResponseEntity<TournamentDto> deleteTournament(@PathVariable Long id) {
    Optional<Tournament> tournament = tournamentService.getTournamentById(id);
    if (tournament.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    TournamentDto deletedTournament = TournamentDto.from(tournament.get());
    tournamentService.deleteTournament(id);
    return ResponseEntity.ok(deletedTournament);
  }

  @PutMapping("/{id}")
  public ResponseEntity<TournamentDto> updateTournament(
      @PathVariable Long id,
      @RequestPart("request") TournamentRequestDto request,
      @RequestPart(value = "imageFile", required = false) MultipartFile imageFile)
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

  @GetMapping("/{id}")
  public ResponseEntity<TournamentDto> getTournament(@PathVariable Long id) {
    return tournamentService
        .getTournamentById(id)
        .map(t -> ResponseEntity.ok(TournamentDto.from(t)))
        .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
  }

  @GetMapping
  public ResponseEntity<TournamentPageDto> getTournamentPage(
      @RequestParam(value = "query", required = false) String query,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size) {
    PageRequest pageable = PageRequest.of(page, size);
    TournamentPageDto tournamentPage =
        TournamentPageDto.fromPage(tournamentService.getTournamentPage(query, pageable));
    return ResponseEntity.ok(tournamentPage);
  }

  @PostMapping("/join")
  public ResponseEntity<TournamentJoinResultDto> joinTournament(
      @RequestBody TournamentJoinRequestDto request, Authentication authentication) {
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

  @GetMapping("/my-tournaments")
  public ResponseEntity<TournamentPageDto> getMyTournaments(
      @RequestParam(value = "query", required = false) String query,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size,
      Authentication authentication) {
    Long currentUserId = userService.getCurrentUser(authentication).getId();
    PageRequest pageable = PageRequest.of(page, size);
    TournamentPageDto tournamentPage =
        TournamentPageDto.fromPage(
            tournamentService.getUserTournamentPage(currentUserId, query, pageable));
    return ResponseEntity.ok(tournamentPage);
  }
}
