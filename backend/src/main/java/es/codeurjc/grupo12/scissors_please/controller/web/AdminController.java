package es.codeurjc.grupo12.scissors_please.controller.web;

import es.codeurjc.grupo12.scissors_please.service.admin.AdminWebHandlerService;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {

  @Autowired private AdminWebHandlerService adminWebHandlerService;

  @GetMapping("/tournaments")
  public String adminTournaments(
      @PageableDefault(size = 10) Pageable pageable,
      @RequestParam(name = "q", required = false) String query,
      @RequestParam(required = false) Integer processed,
      Model model) {
    WebFlowView view = adminWebHandlerService.adminTournamentsHandler(pageable, query, processed);
    view.toModel(model);
    return view.viewName();
  }

  @GetMapping("/tournaments/page")
  public String adminPanelPage(
      @PageableDefault(size = 10) Pageable pageable,
      @RequestParam(name = "q", required = false) String query,
      Model model) {
    WebFlowView view = adminWebHandlerService.adminTournamentsPageHandler(pageable, query);
    view.toModel(model);
    return view.viewName();
  }

  @GetMapping("/tournaments/create")
  public String adminTournamentCreate(@RequestParam(required = false) String success, Model model) {
    WebFlowView view = adminWebHandlerService.adminTournamentCreatePageHandler(success);
    view.toModel(model);
    return view.viewName();
  }

  @PostMapping("/tournaments/create")
  public String createTournament(
      @RequestParam(required = false) String adminTitle,
      @RequestParam(required = false) String adminMaxPlayers,
      @RequestParam(required = false) String adminRegistrationStart,
      @RequestParam(required = false) String adminStartDate,
      @RequestParam(required = false) String adminDescription,
      @RequestParam(required = false) String adminPrize,
      @RequestParam(required = false) MultipartFile image,
      Model model) {
    WebFlowView view =
        adminWebHandlerService.createTournamentHandler(
            adminTitle,
            adminMaxPlayers,
            adminRegistrationStart,
            adminStartDate,
            adminDescription,
            adminPrize,
            image);
    view.toModel(model);
    return view.viewName();
  }

  @GetMapping("/tournaments/edit/{id}")
  public String editTournament(@PathVariable Long id, Model model) {
    WebFlowView view = adminWebHandlerService.editTournamentPageHandler(id);
    view.toModel(model);
    return view.viewName();
  }

  @PostMapping("/tournaments/edit/{id}")
  public String editTournament(
      @PathVariable Long id,
      @RequestParam String name,
      @RequestParam(required = false) String description,
      @RequestParam(required = false) String startDate,
      @RequestParam(required = false) int slots,
      @RequestParam(required = false) MultipartFile image,
      @RequestParam String status,
      Model model,
      Authentication authentication) {
    WebFlowView view =
        adminWebHandlerService.editTournamentHandler(
            id, name, description, startDate, slots, image, status);
    view.toModel(model);
    return view.viewName();
  }

  @GetMapping("/tournaments/detail")
  public String adminTournamentDetail(
      @RequestParam(required = false) Long id,
      @RequestParam(required = false) String runResult,
      Model model) {
    WebFlowView view = adminWebHandlerService.adminTournamentDetailHandler(id, runResult);
    view.toModel(model);
    return view.viewName();
  }

  @PostMapping("/tournaments/process-due")
  public String processDueTournamentsManually() {
    return adminWebHandlerService.processDueTournamentsManuallyHandler().viewName();
  }

  @PostMapping("/tournaments/{id}/run-now")
  public String runTournamentNow(@PathVariable Long id) {
    return adminWebHandlerService.runTournamentNowHandler(id).viewName();
  }

  @PostMapping("/tournaments/{id}/delete")
  public String deleteTournament(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    WebFlowView view = adminWebHandlerService.deleteTournamentHandler(id);
    view.toRedirectAttributes(redirectAttributes);
    return view.viewName();
  }

  @GetMapping("/users")
  public String adminUsers(
      @PageableDefault(size = 10) Pageable pageable,
      @RequestParam(name = "q", required = false) String query,
      @RequestParam(name = "status", required = false) String status,
      Authentication authentication,
      Model model) {
    WebFlowView view =
        adminWebHandlerService.adminUsersHandler(pageable, query, status, authentication);
    view.toModel(model);
    return view.viewName();
  }

  @GetMapping("/users/table")
  public String adminUsersTable(
      @PageableDefault(size = 10) Pageable pageable,
      @RequestParam(name = "q", required = false) String query,
      @RequestParam(name = "status", required = false) String status,
      Authentication authentication,
      Model model) {
    WebFlowView view =
        adminWebHandlerService.adminUsersTableHandler(pageable, query, status, authentication);
    view.toModel(model);
    return view.viewName();
  }

  @PostMapping("/users/{id}/block")
  public String blockUser(
      @PathVariable Long id,
      @RequestParam(name = "q", required = false) String query,
      @RequestParam(name = "status", required = false) String status,
      Authentication authentication,
      RedirectAttributes redirectAttributes) {
    WebFlowView view = adminWebHandlerService.blockUserHandler(id, query, status, authentication);
    view.toRedirectAttributes(redirectAttributes);
    return view.viewName();
  }

  @PostMapping("/users/{id}/unblock")
  public String unblockUser(
      @PathVariable Long id,
      @RequestParam(name = "q", required = false) String query,
      @RequestParam(name = "status", required = false) String status,
      Authentication authentication,
      RedirectAttributes redirectAttributes) {
    WebFlowView view = adminWebHandlerService.unblockUserHandler(id, query, status, authentication);
    view.toRedirectAttributes(redirectAttributes);
    return view.viewName();
  }

  @GetMapping("/bots")
  public String adminBots(
      @PageableDefault(size = 10) Pageable pageable,
      @RequestParam(name = "q", required = false) String query,
      @RequestParam(name = "visibility", required = false, defaultValue = "all") String visibility,
      Model model) {
    WebFlowView view = adminWebHandlerService.adminBotsHandler(pageable, query, visibility);
    view.toModel(model);
    return view.viewName();
  }

  @GetMapping("/bots/table")
  public String adminBotsTable(
      @PageableDefault(size = 10) Pageable pageable,
      @RequestParam(name = "q", required = false) String query,
      @RequestParam(name = "visibility", required = false, defaultValue = "all") String visibility,
      Model model) {
    WebFlowView view = adminWebHandlerService.adminBotsTableHandler(pageable, query, visibility);
    view.toModel(model);
    return view.viewName();
  }

  @PostMapping("/users/{id}/delete")
  public String deleteUser(
      @PathVariable Long id,
      @RequestParam(name = "q", required = false) String query,
      @RequestParam(required = false) String status,
      Authentication authentication,
      RedirectAttributes redirectAttributes) {
    WebFlowView view = adminWebHandlerService.deleteUserHandler(id, query, status, authentication);
    view.toRedirectAttributes(redirectAttributes);
    return view.viewName();
  }
}
