package es.codeurjc.grupo12.scissors_please.controller.web;

import es.codeurjc.grupo12.scissors_please.model.Bot;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.service.BotService;
import es.codeurjc.grupo12.scissors_please.service.MatchService;
import es.codeurjc.grupo12.scissors_please.service.UserService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
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

  @Autowired private BotService botService;
  @Autowired private MatchService matchService;
  @Autowired private UserService userService;

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
  public String matchStats(
      @RequestParam(name = "id", required = false) Long matchId,
      Authentication authentication,
      Model model,
      RedirectAttributes redirectAttributes) {
    try {
      Long currentUserId =
          isAuthenticated(authentication)
              ? userService.getCurrentUser(authentication).getId()
              : null;
      MatchService.MatchStatsView matchStats =
          matchService.getMatchStatsView(matchId, currentUserId);
      if (isAuthenticated(authentication)) {
        matchService.acknowledgeReadyMatch(currentUserId, matchId);
        model.addAttribute("canRematch", matchService.canRequestRematch(matchId, currentUserId));
      }
      model.addAttribute("stats", matchStats);
      model.addAttribute("hasRounds", !matchStats.rounds().isEmpty());
      return "match-stats";
    } catch (IllegalArgumentException exception) {
      redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
      return "redirect:/matches/search";
    }
  }

  @GetMapping("/battle")
  public String matchBattle(
      @RequestParam(name = "id", required = false) Long matchId,
      Authentication authentication,
      Model model,
      RedirectAttributes redirectAttributes) {
    try {
      MatchService.MatchBattleView battleView = matchService.getMatchBattleView(matchId);
      Long userId = userService.getCurrentUser(authentication).getId();
      matchService.acknowledgeReadyMatch(userId, matchId);
      model.addAttribute("battle", battleView);
      return "match-battle";
    } catch (IllegalArgumentException exception) {
      redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
      return "redirect:/matches/search";
    }
  }

  @GetMapping("/search")
  public String matchSearch(
      Authentication authentication,
      @RequestParam(name = "botId", required = false) Long selectedBotId,
      Model model) {
    User currentUser = userService.getCurrentUser(authentication);
    MatchService.MatchmakingStatusView matchmakingStatus =
        matchService.getMatchmakingStatus(currentUser.getId());
    List<Bot> userBots = botService.getBotsForUser(currentUser, true);
    Long effectiveBotId =
        (matchmakingStatus.searching() || matchmakingStatus.matched()) && selectedBotId == null
            ? resolveSelectedBotId(matchmakingStatus.selectedBotId(), userBots)
            : resolveSelectedBotId(selectedBotId, userBots);

    List<BotOption> botOptions =
        userBots.stream()
            .sorted(java.util.Comparator.comparingInt(Bot::getElo).reversed())
            .map(
                bot ->
                    new BotOption(
                        bot.getId(),
                        bot.getName(),
                        bot.getElo(),
                        bot.getId() != null && bot.getId().equals(effectiveBotId)))
            .toList();
    model.addAttribute("hasBots", !botOptions.isEmpty());
    model.addAttribute("botOptions", botOptions);
    model.addAttribute("searching", matchmakingStatus.searching() || matchmakingStatus.matched());
    model.addAttribute("waitingBotName", matchmakingStatus.selectedBotName());
    model.addAttribute("waitingBotElo", matchmakingStatus.selectedBotElo());
    model.addAttribute("waitingBotDescription", matchmakingStatus.selectedBotDescription());
    model.addAttribute("waitingSeconds", matchmakingStatus.waitSeconds());
    model.addAttribute("playersSearching", matchmakingStatus.playersSearching());
    return "match-search";
  }

  @PostMapping("/start")
  public String startMatch(
      Authentication authentication,
      @RequestParam(name = "botId", required = false) Long botId,
      RedirectAttributes redirectAttributes) {
    User currentUser = userService.getCurrentUser(authentication);
    try {
      MatchService.MatchStartResult startResult =
          matchService.startMatchmaking(currentUser.getId(), currentUser.getUsername(), botId);
      if (startResult.matched()) {
        return "redirect:/matches/battle?id=" + startResult.matchId();
      }

      redirectAttributes.addFlashAttribute(
          "successMessage",
          "Searching opponent for " + startResult.myBotName() + ". Waiting for another player...");
      return "redirect:/matches/search";
    } catch (IllegalArgumentException exception) {
      redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
      return "redirect:/matches/search";
    }
  }

  @PostMapping("/cancel")
  public String cancelMatchmaking(
      Authentication authentication, RedirectAttributes redirectAttributes) {
    Long userId = userService.getCurrentUser(authentication).getId();
    try {
      matchService.cancelMatchmaking(userId);
      redirectAttributes.addFlashAttribute("successMessage", "Matchmaking cancelled.");
    } catch (IllegalArgumentException exception) {
      redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
    }
    return "redirect:/matches/search";
  }

  @GetMapping("/search/status")
  @ResponseBody
  public MatchService.MatchmakingStatusView matchmakingStatus(Authentication authentication) {
    Long userId = userService.getCurrentUser(authentication).getId();
    return matchService.getMatchmakingStatus(userId);
  }

  @GetMapping("/rematch/request")
  public String requestRematch(
      Authentication authentication,
      @RequestParam(name = "id", required = false) Long matchId,
      RedirectAttributes redirectAttributes) {
    User currentUser = userService.getCurrentUser(authentication);
    try {
      matchService.requestRematch(matchId, currentUser);
      return "redirect:/matches/search";
    } catch (IllegalArgumentException exception) {
      redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
      return "redirect:/matches/stats?id=" + matchId;
    }
  }

  @GetMapping("/rematch/accept")
  public String acceptRematch(
      Authentication authentication,
      @RequestParam(name = "id", required = false) String invitationId,
      RedirectAttributes redirectAttributes) {
    User currentUser = userService.getCurrentUser(authentication);
    try {
      return "redirect:" + matchService.acceptRematch(invitationId, currentUser);
    } catch (IllegalArgumentException exception) {
      redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
      return "redirect:/home";
    }
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

  private Long resolveSelectedBotId(Long requestedBotId, List<Bot> userBots) {
    if (userBots.isEmpty()) {
      return null;
    }

    if (requestedBotId != null
        && userBots.stream().anyMatch(bot -> requestedBotId.equals(bot.getId()))) {
      return requestedBotId;
    }
    return userBots.get(0).getId();
  }

  private boolean isAuthenticated(Authentication authentication) {
    return authentication != null
        && authentication.isAuthenticated()
        && !(authentication instanceof AnonymousAuthenticationToken);
  }

  private record BotOption(Long id, String name, int elo, boolean selected) {}
}
