package es.codeurjc.grupo12.scissors_please.controller.api.v1.tournaments;

import es.codeurjc.grupo12.scissors_please.dto.TournamentDto;
import es.codeurjc.grupo12.scissors_please.dto.TournamentPageDto;
import es.codeurjc.grupo12.scissors_please.model.Image;
import es.codeurjc.grupo12.scissors_please.model.Tournament;
import es.codeurjc.grupo12.scissors_please.model.TournamentStatus;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.service.image.ImageService;
import es.codeurjc.grupo12.scissors_please.service.tournament.TournamentService;
import es.codeurjc.grupo12.scissors_please.service.user.UserService;
import es.codeurjc.grupo12.scissors_please.views.JoinTournamentResult;

import java.io.IOException;
import java.time.LocalDate;
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
  public ResponseEntity<Void> createTournament(
      @RequestPart TournamentRequest request, @RequestPart("imageFile") MultipartFile imageFile)
      throws IOException {
    Image image = imageService.convertToImage(imageFile);
    tournamentService.createTournament(
        request.name(),
        image,
        request.description(),
        request.slots(),
        request.registrationStarts(),
        request.startDate(),
        request.price());
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteTournament(@PathVariable Long id) {
    tournamentService.deleteTournament(id);
    return ResponseEntity.ok().build();
  }

  @PutMapping("/{id}")
  public ResponseEntity<Void> updateTournament(
      @PathVariable Long id,
      @RequestPart TournamentRequest request,
      @RequestPart("imageFile") MultipartFile imageFile)
      throws IOException {
    Image image = imageService.convertToImage(imageFile);
    Optional<Tournament> tournamentOpt = tournamentService.getTournamentById(id);

    if (tournamentOpt.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    Tournament tournament = tournamentOpt.get();
    tournament.setName(request.name());
    tournament.setDescription(request.description());
    tournament.setStatus(request.status());
    tournament.setImage(image);
    tournament.setSlots(request.slots());
    tournament.setStartDate(request.startDate());
    tournamentService.save(tournament);

    return ResponseEntity.ok().build();
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
    var pageResult = tournamentService.getTournamentPage(query, pageable);

    TournamentPageDto dtoPage = TournamentPageDto.fromPage(pageResult);

    return ResponseEntity.ok(dtoPage);
}
  @GetMapping("/{id}/join")
  public ResponseEntity<TournamentDto> getTournamentJoinPage(
      @PathVariable Long id, Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    User currentUser = userService.getCurrentUser(authentication);
    var tournament = tournamentService.getTournamentJoinPage(id, currentUser);
    return ResponseEntity.ok(TournamentDto.from(tournament));
  }

  @PostMapping("/{id}/join")
  public ResponseEntity<TournamentJoinResultDto> joinTournament(
      @PathVariable Long id,
      @RequestParam(required = false) Long botId,
      Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
    User currentUser = userService.getCurrentUser(authentication);
    JoinTournamentResult result = tournamentService.joinTournament(id, botId, currentUser);

    TournamentJoinResultDto dto = new TournamentJoinResultDto(result.status(), result.message());

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



  private record TournamentRequest(
      String name,
      String description,
      TournamentStatus status,
      int slots,
      LocalDate registrationStarts,
      LocalDate startDate,
      String price) {}
}
