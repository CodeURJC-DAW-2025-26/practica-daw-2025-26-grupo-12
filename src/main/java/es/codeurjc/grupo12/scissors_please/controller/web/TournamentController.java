package es.codeurjc.grupo12.scissors_please.controller.web;

import es.codeurjc.grupo12.scissors_please.model.Tournament;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.service.TournamentService;
import es.codeurjc.grupo12.scissors_please.service.UserService;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/tournaments")
public class TournamentController {

  @Autowired private TournamentService tournamentService;
  @Autowired private UserService userService;

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

  @GetMapping("/detail/{id}")
  public String tournamentDetail(
      Model model, @PathVariable Long id, Authentication authentication) {

    User currentUser =
        isAuthenticated(authentication) ? userService.getCurrentUser(authentication) : null;
    UserType type = resolveUser(currentUser);
    Optional<Tournament> tournamentOp = tournamentService.getTournamentById(id);
    if (tournamentOp.isPresent()) {
      Tournament tournament = tournamentOp.get();
      model.addAttribute("isAdmin", type == UserType.ADMIN);
      model.addAttribute("logged", currentUser != null);
      model.addAttribute("canJoin", currentUser != null && type != UserType.ADMIN);
      model.addAttribute("open", true);
      model.addAttribute("hasTournamentPhoto", tournament.getImage() != null);
      model.addAttribute("participants", tournament.getParticipants().size());
      model.addAttribute("tournament", tournamentOp.get());
      return "tournament-detail";
    }

    return "error";
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

  @GetMapping("/my-tournaments")
  public String myTournaments(
      Authentication authentication,
      @RequestParam(name = "registration", required = false) String registrationFilter,
      @RequestParam(name = "q", required = false) String search,
      Model model) {
    Long userId = userService.getCurrentUser(authentication).getId();
    TournamentService.UserTournamentSection section =
        tournamentService.getUserTournamentSection(userId, registrationFilter, search);

    model.addAttribute("tournaments", section.tournaments());
    model.addAttribute("hasTournaments", !section.tournaments().isEmpty());
    model.addAttribute("search", section.search());
    model.addAttribute("selectedAll", section.selectedAll());
    model.addAttribute("selectedRegistered", section.selectedRegistered());
    model.addAttribute("selectedNotRegistered", section.selectedNotRegistered());
    return "my-tournaments-auth";
  }

  private enum UserType {
    GUEST,
    USER,
    ADMIN,
  }

  private UserType resolveUser(User user) {
    if (user == null) {
      return UserType.GUEST;
    }
    if (hasRole(user, "ADMIN")) {
      return UserType.ADMIN;
    }
    if (hasRole(user, "USER")) {
      return UserType.USER;
    }
    return UserType.GUEST;
  }

  private boolean hasRole(User user, String role) {
    return user.getRoles() != null && user.getRoles().contains(role);
  }

  private boolean isAuthenticated(Authentication authentication) {
    return authentication != null
        && authentication.isAuthenticated()
        && !(authentication instanceof AnonymousAuthenticationToken);
  }
}
