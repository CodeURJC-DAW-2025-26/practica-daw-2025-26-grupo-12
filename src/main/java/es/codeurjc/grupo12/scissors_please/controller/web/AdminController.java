package es.codeurjc.grupo12.scissors_please.controller.web;

import es.codeurjc.grupo12.scissors_please.service.TournamentAutomationService;
import es.codeurjc.grupo12.scissors_please.service.TournamentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

  private final TournamentService tournamentService;
  private final TournamentAutomationService tournamentAutomationService;

  @GetMapping("/panel")
  public String adminPanel(
      @PageableDefault(size = 5) Pageable pageable,
      @RequestParam(required = false) Integer processed,
      Model model) {
    model.addAttribute("size", Math.max(pageable.getPageSize(), 1));
    model.addAttribute("fromItem", 0);
    model.addAttribute("toItem", 0);
    model.addAttribute("totalElements", 0);
    if (processed != null) {
      if (processed > 0) {
        model.addAttribute("successMessage", "Processed " + processed + " tournament(s).");
      } else {
        model.addAttribute("successMessage", "No upcoming tournaments to process.");
      }
    }
    return "admin-panel";
  }

  @GetMapping("/panel/page")
  public String adminPanelPage(@PageableDefault(size = 5) Pageable pageable, Model model) {
    TournamentService.TournamentPage tournamentPage = tournamentService.getTournamentPage(pageable);
    model.addAttribute("tournaments", tournamentPage.tournaments());
    model.addAttribute(
        "showEmpty", pageable.getPageNumber() == 0 && tournamentPage.tournaments().isEmpty());
    model.addAttribute("nextPage", tournamentPage.nextPage());
    model.addAttribute("hasMore", tournamentPage.hasMore());
    model.addAttribute("totalElements", tournamentPage.totalElements());
    model.addAttribute("fromItem", tournamentPage.fromItem());
    model.addAttribute("toItem", tournamentPage.toItem());
    return "components/admin-tournament-page-chunk";
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
  public String adminTournamentDetail(
      @RequestParam(required = false) Long id,
      @RequestParam(required = false) String runResult,
      Model model) {
    TournamentService.AdminTournamentDetail tournament =
        tournamentService.getAdminTournamentDetail(id);
    model.addAttribute("tournament", tournament);
    if (runResult != null) {
      switch (runResult) {
        case "executed" ->
            model.addAttribute("successMessage", "Tournament executed successfully.");
        case "not-upcoming" ->
            model.addAttribute("errorMessage", "Only upcoming tournaments can be executed now.");
        case "not-found" -> model.addAttribute("errorMessage", "Tournament not found.");
        default -> {
          // no-op
        }
      }
    }
    return "admin-tournament-detail";
  }

  @PostMapping("/tournaments/process-due")
  public String processDueTournamentsManually() {
    int processed = tournamentAutomationService.processDueUpcomingTournamentsNow();
    return "redirect:/admin/panel?processed=" + processed;
  }

  @PostMapping("/tournament/{id}/run-now")
  public String runTournamentNow(@org.springframework.web.bind.annotation.PathVariable Long id) {
    TournamentAutomationService.RunNowResult result =
        tournamentAutomationService.runTournamentNow(id);
    return switch (result) {
      case EXECUTED -> "redirect:/admin/tournament/detail?id=" + id + "&runResult=executed";
      case NOT_UPCOMING -> "redirect:/admin/tournament/detail?id=" + id + "&runResult=not-upcoming";
      case NOT_FOUND -> "redirect:/admin/tournament/detail?runResult=not-found";
    };
  }
}
