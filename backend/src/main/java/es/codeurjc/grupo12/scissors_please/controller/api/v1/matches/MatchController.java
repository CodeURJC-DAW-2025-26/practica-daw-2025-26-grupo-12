package es.codeurjc.grupo12.scissors_please.controller.api.v1.matches;

import es.codeurjc.grupo12.scissors_please.config.ResponseConstants;
import es.codeurjc.grupo12.scissors_please.dto.ResponseDto;
import es.codeurjc.grupo12.scissors_please.service.match.MatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("apiMatchController")
@RequestMapping("/api/v1/matches")
public class MatchController {

  @Autowired MatchService matchService;

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
  
}
