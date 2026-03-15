package es.codeurjc.grupo12.scissors_please.controller.web;

import es.codeurjc.grupo12.scissors_please.service.tournament.TournamentWebHandlerService;
import es.codeurjc.grupo12.scissors_please.views.WebFlowView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/tournaments")
public class TournamentController {

  @Autowired private TournamentWebHandlerService tournamentWebHandlerService;

  @GetMapping
  public String tournamentList(
      @PageableDefault(size = 10) Pageable pageable,
      @RequestParam(name = "q", required = false) String query,
      Model model) {
    WebFlowView view = tournamentWebHandlerService.tournamentListHandler(pageable, query);
    view.toModel(model);
    return view.viewName();
  }

  @GetMapping("/page")
  public String tournamentListPage(
      @PageableDefault(size = 10) Pageable pageable,
      @RequestParam(name = "q", required = false) String query,
      Model model) {
    WebFlowView view = tournamentWebHandlerService.tournamentListPageHandler(pageable, query);
    view.toModel(model);
    return view.viewName();
  }

  @GetMapping("/detail/{id}")
  public String tournamentDetail(
      Model model, @PathVariable Long id, Authentication authentication) {
    WebFlowView view = tournamentWebHandlerService.tournamentDetailHandler(id, authentication);
    view.toModel(model);
    return view.viewName();
  }

  @GetMapping("/create")
  public String tournamentCreate(Model model) {
    WebFlowView view = tournamentWebHandlerService.tournamentCreateHandler();
    view.toModel(model);
    return view.viewName();
  }

  @GetMapping("/join")
  public String tournamentJoinRedirect() {
    return tournamentWebHandlerService.tournamentJoinRedirectHandler().viewName();
  }

  @GetMapping("/join/{id}")
  public String tournamentJoin(@PathVariable Long id, Authentication authentication, Model model) {
    WebFlowView view = tournamentWebHandlerService.tournamentJoinHandler(id, authentication);
    view.toModel(model);
    return view.viewName();
  }

  @PostMapping("/join/{id}")
  public String joinTournament(
      @PathVariable Long id,
      @RequestParam(required = false) Long botId,
      Authentication authentication,
      RedirectAttributes redirectAttributes) {
    WebFlowView view = tournamentWebHandlerService.joinTournamentHandler(id, botId, authentication);
    view.toRedirectAttributes(redirectAttributes);
    return view.viewName();
  }

  @GetMapping("/my-tournaments")
  public String myTournaments(
      Authentication authentication,
      @RequestParam(name = "q", required = false) String search,
      Model model) {
    WebFlowView view = tournamentWebHandlerService.myTournamentsHandler(authentication, search);
    view.toModel(model);
    return view.viewName();
  }
}
