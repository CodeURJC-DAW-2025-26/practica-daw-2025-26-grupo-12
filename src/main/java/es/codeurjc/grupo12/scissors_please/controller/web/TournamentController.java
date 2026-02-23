package es.codeurjc.grupo12.scissors_please.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/tournaments")
public class TournamentController {

  @GetMapping("/list")
  public String tournamentList() {
    return "tournament-list";
  }

  @GetMapping("/detail")
  public String tournamentDetail() {
    return "tournament-detail";
  }

  @GetMapping("/detail-open")
  public String tournamentDetailOpen() {
    return "tournament-detail-open";
  }

  @GetMapping("/create")
  public String tournamentCreate() {
    return "tournament-create";
  }

  @GetMapping("/join")
  public String tournamentJoin() {
    return "tournament-join";
  }

  @GetMapping("/results")
  public String tournamentResults() {
    return "tournament-results";
  }
}
