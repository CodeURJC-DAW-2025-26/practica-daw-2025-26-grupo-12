package es.codeurjc.grupo12.scissors_please.controller.web;

import es.codeurjc.grupo12.scissors_please.service.MatchService;
import es.codeurjc.grupo12.scissors_please.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/matches")
@RequiredArgsConstructor
public class MatchController {

  private final MatchService matchService;
  private final UserService userService;

  @GetMapping("/list")
  public String matchList(@PageableDefault(size = 5) Pageable pageable, Model model) {
    model.addAttribute("size", Math.max(pageable.getPageSize(), 1));
    model.addAttribute("fromItem", 0);
    model.addAttribute("toItem", 0);
    model.addAttribute("totalElements", 0);
    return "match-list";
  }

  @GetMapping("/list/page")
  public String matchListPage(@PageableDefault(size = 5) Pageable pageable, Model model) {
    MatchService.MatchPage matchPage = matchService.getBestMatchPage(pageable);
    model.addAttribute("matches", matchPage.matches());
    model.addAttribute("showEmpty", pageable.getPageNumber() == 0 && matchPage.matches().isEmpty());
    model.addAttribute("nextPage", matchPage.nextPage());
    model.addAttribute("hasMore", matchPage.hasMore());
    model.addAttribute("totalElements", matchPage.totalElements());
    model.addAttribute("fromItem", matchPage.fromItem());
    model.addAttribute("toItem", matchPage.toItem());
    return "components/match-page-chunk";
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
  public String recentMatches(
      Authentication authentication,
      @RequestParam(name = "participation", required = false) String participationFilter,
      Model model) {
    Long userId = userService.getCurrentUser(authentication).getId();
    MatchService.UserRecentMatchSection section =
        matchService.getUserRecentMatchSection(userId, participationFilter);

    model.addAttribute("matches", section.matches());
    model.addAttribute("hasMatches", !section.matches().isEmpty());
    model.addAttribute("selectedAll", section.selectedAll());
    model.addAttribute("selectedPlayed", section.selectedPlayed());
    model.addAttribute("selectedNotPlayed", section.selectedNotPlayed());
    return "recent-matches";
  }
}
