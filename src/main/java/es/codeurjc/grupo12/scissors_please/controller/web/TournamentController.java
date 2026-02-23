package es.codeurjc.grupo12.scissors_please.controller.web;

import es.codeurjc.grupo12.scissors_please.service.TournamentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/tournaments")
@RequiredArgsConstructor
public class TournamentController {

  private final TournamentService tournamentService;

  @GetMapping
  public String tournamentList(@PageableDefault(size = 5) Pageable pageable, Model model) {
    model.addAttribute("size", Math.max(pageable.getPageSize(), 1));
    model.addAttribute("fromItem", 0);
    model.addAttribute("toItem", 0);
    model.addAttribute("totalElements", 0);
    return "tournament-list";
  }

  @GetMapping("/page")
  public String tournamentListPage(@PageableDefault(size = 5) Pageable pageable, Model model) {
    TournamentService.TournamentPage tournamentPage = tournamentService.getTournamentPage(pageable);
    model.addAttribute("tournaments", tournamentPage.tournaments());
    model.addAttribute(
        "showEmpty", pageable.getPageNumber() == 0 && tournamentPage.tournaments().isEmpty());
    model.addAttribute("nextPage", tournamentPage.nextPage());
    model.addAttribute("hasMore", tournamentPage.hasMore());
    model.addAttribute("totalElements", tournamentPage.totalElements());
    model.addAttribute("fromItem", tournamentPage.fromItem());
    model.addAttribute("toItem", tournamentPage.toItem());
    return "components/tournament-page-chunk";
  }

  @GetMapping("/detail")
  public String tournamentDetail(org.springframework.ui.Model model) {
    model.addAttribute("open", true);
    return "tournament-detail";
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
