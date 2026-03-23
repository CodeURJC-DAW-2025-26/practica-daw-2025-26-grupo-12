package es.codeurjc.grupo12.scissors_please.controller.api.v1.tournaments;

import es.codeurjc.grupo12.scissors_please.config.ResponseConstants;
import es.codeurjc.grupo12.scissors_please.dto.ResponseDto;
import es.codeurjc.grupo12.scissors_please.model.Image;
import es.codeurjc.grupo12.scissors_please.model.Tournament;
import es.codeurjc.grupo12.scissors_please.model.TournamentStatus;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.service.image.ImageService;
import es.codeurjc.grupo12.scissors_please.service.tournament.TournamentService;
import es.codeurjc.grupo12.scissors_please.service.user.UserService;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
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

  @Autowired TournamentService tournamentService;
  @Autowired ImageService imageService;
  @Autowired UserService userService;

  @PostMapping
  public ResponseDto createTournament(
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

    return new ResponseDto(false, ResponseConstants.OK_CODE_INT, ResponseConstants.OK, null);
  }

  @DeleteMapping("/{id}")
  ResponseDto deleteTournament(@PathVariable Long id) {
    tournamentService.deleteTournament(id);
    return new ResponseDto(false, ResponseConstants.OK_CODE_INT, ResponseConstants.OK, null);
  }

  @PutMapping("/{id}")
  public ResponseDto updateTournament(
      @PathVariable Long id,
      @RequestPart TournamentRequest request,
      @RequestPart("imageFile") MultipartFile imageFile)
      throws IOException {
    Image image = imageService.convertToImage(imageFile);
    Optional<Tournament> tournament = tournamentService.getTournamentById(id);
    if (!tournament.isPresent()) {
      return new ResponseDto(
          true, ResponseConstants.NOT_FOUND_CODE_INT, ResponseConstants.TOURNAMENT_NOT_FOUND, null);
    }
    Tournament newTournament = tournament.get();
    newTournament.setName(request.name());
    newTournament.setDescription(request.description());
    newTournament.setStatus(request.status());
    newTournament.setImage(image);
    newTournament.setSlots(request.slots());
    newTournament.setStartDate(request.startDate());
    tournamentService.save(newTournament);

    return new ResponseDto(false, ResponseConstants.OK_CODE_INT, ResponseConstants.OK, null);
  }

  @GetMapping("/{id}")
  public ResponseDto getTournament(@PathVariable Long id) {
    return tournamentService
        .getTournamentById(id)
        .map(
            tournament ->
                new ResponseDto(
                    false, ResponseConstants.OK_CODE_INT, ResponseConstants.OK, tournament))
        .orElse(
            new ResponseDto(
                true,
                ResponseConstants.NOT_FOUND_CODE_INT,
                ResponseConstants.ELEMENT_NOT_FOUND,
                null));
  }

  @GetMapping
  public ResponseDto getTournamentPage(
      @RequestParam(value = "query", required = false) String query,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size) {

    PageRequest pageable = PageRequest.of(page, size);
    var tournamentPage = tournamentService.getTournamentPage(query, pageable);

    return new ResponseDto(
        false, ResponseConstants.OK_CODE_INT, ResponseConstants.OK, tournamentPage);
  }

  @GetMapping("/{id}/join")
  public ResponseDto getTournamentJoinPage(@PathVariable Long id, Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return new ResponseDto(
          true, ResponseConstants.UNAUTHORIZED_CODE_INT, ResponseConstants.ACCESS_DENIED, null);
    }

    User currentUser = userService.getCurrentUser(authentication);
    var joinPage = tournamentService.getTournamentJoinPage(id, currentUser);

    return new ResponseDto(false, ResponseConstants.OK_CODE_INT, ResponseConstants.OK, joinPage);
  }

  @PostMapping("/{id}/join")
  public ResponseDto joinTournament(
      @PathVariable Long id,
      @RequestParam(required = false) Long botId,
      Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return new ResponseDto(
          true, ResponseConstants.UNAUTHORIZED_CODE_INT, ResponseConstants.ACCESS_DENIED, null);
    }

    User currentUser = userService.getCurrentUser(authentication);
    var result = tournamentService.joinTournament(id, botId, currentUser);

    return switch (result.status()) {
      case JOINED ->
          new ResponseDto(
              false, ResponseConstants.OK_CODE_INT, ResponseConstants.OK, result.message());
      case TOURNAMENT_NOT_FOUND ->
          new ResponseDto(true, ResponseConstants.NOT_FOUND_CODE_INT, result.message(), null);
      case INVALID_USER, ADMIN_NOT_ALLOWED ->
          new ResponseDto(true, ResponseConstants.FORBIDDEN_CODE_INT, result.message(), null);
      case INVALID_BOT ->
          new ResponseDto(true, ResponseConstants.BAD_REQUEST_CODE_INT, result.message(), null);
      case ALREADY_REGISTERED,
          BOT_ALREADY_REGISTERED,
          REGISTRATION_CLOSED,
          REGISTRATION_NOT_OPEN,
          TOURNAMENT_FULL ->
          new ResponseDto(true, ResponseConstants.CONFLICT_CODE_INT, result.message(), null);
    };
  }

  @GetMapping("/my-tournaments")
  public ResponseDto getMyTournaments(
      @RequestParam(name = "q", required = false) String query, Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return new ResponseDto(
          true, ResponseConstants.UNAUTHORIZED_CODE_INT, ResponseConstants.ACCESS_DENIED, null);
    }

    User currentUser = userService.getCurrentUser(authentication);
    var section = tournamentService.getUserTournamentSection(currentUser.getId(), query);

    return new ResponseDto(false, ResponseConstants.OK_CODE_INT, ResponseConstants.OK, section);
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
