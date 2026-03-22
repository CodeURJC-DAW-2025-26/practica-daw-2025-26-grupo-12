package es.codeurjc.grupo12.scissors_please.controller.api.v1.tournaments;

import es.codeurjc.grupo12.scissors_please.config.ResponseConstants;
import es.codeurjc.grupo12.scissors_please.dto.ResponseDto;
import es.codeurjc.grupo12.scissors_please.model.Image;
import es.codeurjc.grupo12.scissors_please.model.TournamentStatus;
import es.codeurjc.grupo12.scissors_please.service.image.ImageService;
import es.codeurjc.grupo12.scissors_please.service.tournament.TournamentService;
import java.io.IOException;
import java.time.LocalDate;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController("apiTournamentController")
@RequestMapping("/api/v1/tournaments")
@Log
public class TournamentController {

  @Autowired TournamentService tournamentService;
  @Autowired ImageService imageService;

  @PostMapping()
  public ResponseDto createTournament(
      @RequestPart CreateTournamentRequest request,
      @RequestPart("imageFile") MultipartFile imageFile)
      throws IOException {
    log.info("Recibimos la Request");
    Image image = imageService.convertToImage(imageFile);

    tournamentService.createTournament(
        request.name(),
        image,
        request.description(),
        request.slots(),
        request.registrationStarts(),
        request.startDate(),
        request.price());

    return new ResponseDto(false, ResponseConstants.OK_CODE_INT, ResponseConstants.OK);
  }

  private record CreateTournamentRequest(
      String name,
      String description,
      TournamentStatus status,
      int slots,
      LocalDate registrationStarts,
      LocalDate startDate,
      String price) {}
}
