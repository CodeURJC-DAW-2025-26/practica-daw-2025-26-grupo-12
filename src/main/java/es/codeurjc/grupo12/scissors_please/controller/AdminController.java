package es.codeurjc.grupo12.scissors_please.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

  @GetMapping("/panel")
  public String adminPanel() {
    return "admin-panel-admin";
  }

  @GetMapping("/home")
  public String adminHome() {
    return "home-admin";
  }

  @GetMapping("/tournament/create")
  public String adminTournamentCreate() {
    return "admin-tournament-create-admin";
  }

  @GetMapping("/tournament/edit")
  public String adminTournamentEdit() {
    return "admin-tournament-edit-admin";
  }

  @GetMapping("/tournament/detail")
  public String adminTournamentDetail() {
    return "admin-tournament-detail-admin";
  }
}
