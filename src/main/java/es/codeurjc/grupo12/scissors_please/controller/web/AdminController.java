package es.codeurjc.grupo12.scissors_please.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

  @GetMapping("/panel")
  public String adminPanel() {
    return "admin-panel";
  }

  @GetMapping("/home")
  public String adminHome() {
    return "home-admin";
  }

  @GetMapping("/tournament/create")
  public String adminTournamentCreate() {
    return "admin-tournament-create";
  }

  @GetMapping("/tournament/edit")
  public String adminTournamentEdit() {
    return "admin-tournament-edit";
  }

  @GetMapping("/tournament/detail")
  public String adminTournamentDetail() {
    return "admin-tournament-detail";
  }
}
