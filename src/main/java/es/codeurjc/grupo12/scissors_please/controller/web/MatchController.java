package es.codeurjc.grupo12.scissors_please.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/matches")
public class MatchController {

  @GetMapping("/list")
  public String matchList() {
    return "match-list";
  }

  @GetMapping("/stats")
  public String matchStats() {
    return "match-stats";
  }

  @GetMapping("/battle")
  public String matchBattle() {
    return "match-battle";
  }

  @GetMapping("/search")
  public String matchSearch() {
    return "match-search";
  }

  @GetMapping("/recent")
  public String recentMatches() {
    return "recent-matches";
  }
}
