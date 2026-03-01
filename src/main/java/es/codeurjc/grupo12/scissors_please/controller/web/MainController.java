package es.codeurjc.grupo12.scissors_please.controller.web;

import es.codeurjc.grupo12.scissors_please.model.Bot;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.service.BotService;
import es.codeurjc.grupo12.scissors_please.service.ChartService;
import es.codeurjc.grupo12.scissors_please.service.MatchService;
import es.codeurjc.grupo12.scissors_please.service.TournamentService;
import es.codeurjc.grupo12.scissors_please.service.UserService;
import es.codeurjc.grupo12.scissors_please.service.UserService.UserStats;
import java.awt.Color;
import java.util.Base64;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

  @Autowired private BotService botService;
  @Autowired private MatchService matchService;
  @Autowired private TournamentService tournamentService;
  @Autowired private UserService userService;
  @Autowired private ChartService chartService;

  @GetMapping("/")
  public String index() {
    return "redirect:/home";
  }

  @GetMapping("/home")
  public String home(Authentication authentication, Model model) {
    User currentUser =
        isAuthenticated(authentication) ? userService.getCurrentUser(authentication) : null;
    HomeMode homeMode = resolveHomeMode(currentUser);

    if (homeMode == HomeMode.USER) {
      List<Bot> topBots = botService.getTopBotsForUser(currentUser, true, 3);
      List<MatchService.UserMatchItem> recentMatches =
          matchService.getUserHomeMatches(currentUser.getId(), 3);
      List<TournamentService.UserTournamentItem> myTournaments =
          tournamentService.getUserHomeTournaments(currentUser.getId(), 3);
      BotService.UserGlobalRanking userGlobalRanking = botService.getUserGlobalRanking(currentUser);
      List<Bot> allUserBots = botService.getBotsForUser(currentUser, true);

      UserStats stats = userService.getTotalStats(currentUser);
      int maxElo = userService.getMaxElo(currentUser);
      byte[] winRateImg =
          chartService.generateProgressBar(
              stats.totalWins(), stats.totalMatches(), new Color(34, 197, 94));
      byte[] eloImg = chartService.generateProgressBar(maxElo, 2500, new Color(139, 92, 246));

      model.addAttribute("winRateBar", Base64.getEncoder().encodeToString(winRateImg));
      model.addAttribute("winRateValue", stats.getWinRate());
      model.addAttribute("eloBar", Base64.getEncoder().encodeToString(eloImg));

      int currentElo = userGlobalRanking.bestElo();
      int maxEloGoal = 3000;
      byte[] eloChart =
          chartService.generateProgressBar(currentElo, maxEloGoal, new Color(139, 92, 246));
      model.addAttribute("eloBar", Base64.getEncoder().encodeToString(eloChart));
      model.addAttribute("currentBestElo", currentElo);

      model.addAttribute("hasProfilePhoto", currentUser.getImage() != null);
      model.addAttribute("profileId", currentUser.getId());
      model.addAttribute("currentUsername", currentUser.getUsername());
      model.addAttribute("currentUserInitial", resolveInitial(currentUser.getUsername()));
      model.addAttribute("totalBotCount", allUserBots.size());
      model.addAttribute("hasRanking", userGlobalRanking.ranked());
      model.addAttribute("globalRank", userGlobalRanking.rank());
      model.addAttribute("globalRankingTotal", userGlobalRanking.totalUsers());
      model.addAttribute("currentBestElo", userGlobalRanking.bestElo());

      model.addAttribute("topBots", topBots);
      model.addAttribute("hasBots", !topBots.isEmpty());
      model.addAttribute("recentMatches", recentMatches);
      model.addAttribute("hasRecentMatches", !recentMatches.isEmpty());
      model.addAttribute("myTournaments", myTournaments);
      model.addAttribute("hasMyTournaments", !myTournaments.isEmpty());
    }

    model.addAttribute(
        "homeMode",
        new HomeModeView(
            homeMode == HomeMode.GUEST, homeMode == HomeMode.USER, homeMode == HomeMode.ADMIN));
    model.addAttribute("logged", homeMode != HomeMode.GUEST);
    model.addAttribute("admin", homeMode == HomeMode.ADMIN);
    return "home";
  }

  private boolean isAuthenticated(Authentication authentication) {
    return authentication != null
        && authentication.isAuthenticated()
        && !(authentication instanceof AnonymousAuthenticationToken);
  }

  private boolean hasRole(User user, String role) {
    return user.getRoles() != null && user.getRoles().contains(role);
  }

  private HomeMode resolveHomeMode(User user) {
    if (user == null) {
      return HomeMode.GUEST;
    }
    if (hasRole(user, "ADMIN")) {
      return HomeMode.ADMIN;
    }
    if (hasRole(user, "USER")) {
      return HomeMode.USER;
    }
    return HomeMode.GUEST;
  }

  private String resolveInitial(String value) {
    if (value == null || value.isBlank()) {
      return "?";
    }
    return value.substring(0, 1).toUpperCase();
  }

  private enum HomeMode {
    GUEST,
    USER,
    ADMIN
  }

  public record HomeModeView(boolean guest, boolean user, boolean admin) {}
}
