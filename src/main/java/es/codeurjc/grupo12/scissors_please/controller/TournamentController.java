package es.codeurjc.grupo12.scissors_please.controller;

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

  @GetMapping("/list-auth")
  public String tournamentListAuth() {
    return "tournament-list-auth";
  }

  @GetMapping("/detail")
  public String tournamentDetail() {
    return "tournament-detail";
  }

  @GetMapping("/detail-auth")
  public String tournamentDetailAuth() {
    return "tournament-detail-auth";
  }

  @GetMapping("/detail-open")
  public String tournamentDetailOpen() {
    return "tournament-detail-open";
  }

  @GetMapping("/detail-open-auth")
  public String tournamentDetailOpenAuth() {
    return "tournament-detail-open-auth";
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

  @GetMapping("/results-auth")
  public String tournamentResultsAuth() {
    return "tournament-results-auth";
  }
}
