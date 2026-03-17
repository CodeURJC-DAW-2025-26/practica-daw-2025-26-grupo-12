package es.codeurjc.grupo12.scissors_please.controller.web;

import es.codeurjc.grupo12.scissors_please.service.match.MatchWebHandlerService;
import es.codeurjc.grupo12.scissors_please.views.MatchmakingStatusView;
import es.codeurjc.grupo12.scissors_please.views.WebFlowView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/matches")
public class MatchController {

  @Autowired private MatchWebHandlerService matchWebHandlerService;

  @GetMapping("/list")
  public String matchList(@PageableDefault(size = 10) Pageable pageable, Model model) {
    WebFlowView view = matchWebHandlerService.matchListHandler(pageable);
    view.toModel(model);
    return view.viewName();
  }

  @GetMapping("/list/page")
  public String matchListPage(@PageableDefault(size = 10) Pageable pageable, Model model) {
    WebFlowView view = matchWebHandlerService.matchListPageHandler(pageable);
    view.toModel(model);
    return view.viewName();
  }

  @GetMapping("/stats")
  public String matchStats(
      @RequestParam(name = "id", required = false) Long matchId,
      Authentication authentication,
      Model model,
      RedirectAttributes redirectAttributes) {
    WebFlowView view = matchWebHandlerService.matchStatsHandler(matchId, authentication);
    view.toModel(model);
    view.toRedirectAttributes(redirectAttributes);
    return view.viewName();
  }

  @GetMapping("/battle")
  public String matchBattle(
      @RequestParam(name = "id", required = false) Long matchId,
      Authentication authentication,
      Model model,
      RedirectAttributes redirectAttributes) {
    WebFlowView view = matchWebHandlerService.matchBattleHandler(matchId, authentication);
    view.toModel(model);
    view.toRedirectAttributes(redirectAttributes);
    return view.viewName();
  }

  @GetMapping("/search")
  public String matchSearch(
      Authentication authentication,
      @RequestParam(name = "botId", required = false) Long selectedBotId,
      Model model) {
    WebFlowView view = matchWebHandlerService.matchSearchHandler(authentication, selectedBotId);
    view.toModel(model);
    return view.viewName();
  }

  @PostMapping("/start")
  public String startMatch(
      Authentication authentication,
      @RequestParam(name = "botId", required = false) Long botId,
      RedirectAttributes redirectAttributes) {
    WebFlowView view = matchWebHandlerService.startMatchHandler(authentication, botId);
    view.toRedirectAttributes(redirectAttributes);
    return view.viewName();
  }

  @PostMapping("/cancel")
  public String cancelMatchmaking(
      Authentication authentication, RedirectAttributes redirectAttributes) {
    WebFlowView view = matchWebHandlerService.cancelMatchmakingHandler(authentication);
    view.toRedirectAttributes(redirectAttributes);
    return view.viewName();
  }

  @GetMapping("/search/status")
  @ResponseBody
  public MatchmakingStatusView matchmakingStatus(Authentication authentication) {
    return matchWebHandlerService.matchmakingStatusHandler(authentication);
  }

  @GetMapping("/rematch/request")
  public String requestRematch(
      Authentication authentication,
      @RequestParam(name = "id", required = false) Long matchId,
      RedirectAttributes redirectAttributes) {
    WebFlowView view = matchWebHandlerService.requestRematchHandler(authentication, matchId);
    view.toRedirectAttributes(redirectAttributes);
    return view.viewName();
  }

  @GetMapping("/rematch/accept")
  public String acceptRematch(
      Authentication authentication,
      @RequestParam(name = "id", required = false) String invitationId,
      RedirectAttributes redirectAttributes) {
    WebFlowView view = matchWebHandlerService.acceptRematchHandler(authentication, invitationId);
    view.toRedirectAttributes(redirectAttributes);
    return view.viewName();
  }

  @GetMapping("/recent")
  public String recentMatches(Authentication authentication, Model model) {
    WebFlowView view = matchWebHandlerService.recentMatchesHandler(authentication);
    view.toModel(model);
    return view.viewName();
  }
}
