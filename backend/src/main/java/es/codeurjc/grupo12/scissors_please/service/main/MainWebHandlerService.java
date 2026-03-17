package es.codeurjc.grupo12.scissors_please.service.main;

import es.codeurjc.grupo12.scissors_please.model.Bot;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.service.bot.BotService;
import es.codeurjc.grupo12.scissors_please.service.chart.ChartService;
import es.codeurjc.grupo12.scissors_please.service.match.MatchService;
import es.codeurjc.grupo12.scissors_please.service.tournament.TournamentService;
import es.codeurjc.grupo12.scissors_please.service.user.UserService;
import es.codeurjc.grupo12.scissors_please.service.user.UserService.UserStats;
import es.codeurjc.grupo12.scissors_please.views.HomeModeView;
import es.codeurjc.grupo12.scissors_please.views.UserGlobalRanking;
import es.codeurjc.grupo12.scissors_please.views.UserMatchItem;
import es.codeurjc.grupo12.scissors_please.views.UserTournamentItem;
import es.codeurjc.grupo12.scissors_please.views.WebFlowView;
import es.codeurjc.grupo12.scissors_please.views.WebPageView;
import es.codeurjc.grupo12.scissors_please.views.WebRedirectView;
import java.awt.Color;
import java.util.Base64;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class MainWebHandlerService {

  @Autowired private BotService botService;
  @Autowired private MatchService matchService;
  @Autowired private TournamentService tournamentService;
  @Autowired private UserService userService;
  @Autowired private ChartService chartService;

  public WebFlowView indexHandler() {
    return WebRedirectView.to("/home");
  }

  public WebFlowView homeHandler(Authentication authentication) {
    User currentUser =
        isAuthenticated(authentication) ? userService.getCurrentUser(authentication) : null;
    HomeMode homeMode = resolveHomeMode(currentUser);
    WebPageView view =
        WebPageView.of("home")
            .attribute(
                "homeMode",
                new HomeModeView(
                    homeMode == HomeMode.GUEST,
                    homeMode == HomeMode.USER,
                    homeMode == HomeMode.ADMIN))
            .attribute("logged", homeMode != HomeMode.GUEST)
            .attribute("admin", homeMode == HomeMode.ADMIN);

    if (homeMode != HomeMode.USER) {
      return view;
    }

    List<Bot> topBots = botService.getTopBotsForUser(currentUser, true, 3);
    List<UserMatchItem> recentMatches = matchService.getUserHomeMatches(currentUser.getId(), 3);
    List<UserTournamentItem> myTournaments =
        tournamentService.getUserHomeTournaments(currentUser.getId(), 3);
    UserGlobalRanking userGlobalRanking = botService.getUserGlobalRanking(currentUser);
    List<Bot> allUserBots = botService.getBotsForUser(currentUser, true);

    UserStats stats = userService.getTotalStats(currentUser);
    byte[] winRateImg =
        chartService.generateProgressBar(
            stats.totalWins(), stats.totalMatches(), new Color(34, 197, 94));

    int currentElo = userGlobalRanking.bestElo();
    byte[] eloChart = chartService.generateProgressBar(currentElo, 3000, new Color(139, 92, 246));

    return view.attribute("winRateBar", Base64.getEncoder().encodeToString(winRateImg))
        .attribute("winRateValue", stats.getWinRate())
        .attribute("eloBar", Base64.getEncoder().encodeToString(eloChart))
        .attribute("currentBestElo", currentElo)
        .attribute("hasProfilePhoto", currentUser.getImage() != null)
        .attribute("profileId", currentUser.getId())
        .attribute("currentUsername", currentUser.getUsername())
        .attribute("currentUserInitial", resolveInitial(currentUser.getUsername()))
        .attribute("totalBotCount", allUserBots.size())
        .attribute("hasRanking", userGlobalRanking.ranked())
        .attribute("globalRank", userGlobalRanking.rank())
        .attribute("globalRankingTotal", userGlobalRanking.totalUsers())
        .attribute("currentBestElo", userGlobalRanking.bestElo())
        .attribute("topBots", topBots)
        .attribute("hasBots", !topBots.isEmpty())
        .attribute("recentMatches", recentMatches)
        .attribute("hasRecentMatches", !recentMatches.isEmpty())
        .attribute("myTournaments", myTournaments)
        .attribute("hasMyTournaments", !myTournaments.isEmpty());
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
}
