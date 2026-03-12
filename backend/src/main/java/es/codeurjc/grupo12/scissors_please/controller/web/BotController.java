package es.codeurjc.grupo12.scissors_please.controller.web;

import es.codeurjc.grupo12.scissors_please.model.Bot;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.service.BotService;
import es.codeurjc.grupo12.scissors_please.service.ChartService;
import es.codeurjc.grupo12.scissors_please.service.UserService;
import java.util.Base64;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/bots")
public class BotController {

  @Autowired private BotService botService;
  @Autowired private UserService userService;
  @Autowired private ChartService chartService;

  @PostMapping
  public String createBot(
      @RequestParam String name,
      @RequestParam(required = false) String description,
      @RequestParam(required = false) String tags,
      @RequestParam(required = false) MultipartFile image,
      @RequestParam(defaultValue = "false") boolean isPublic,
      Authentication authentication) {
    User currentUser = userService.getCurrentUser(authentication);

    Bot bot = botService.createBot(currentUser, name, description, tags, image, isPublic);

    return redirectToBotEdit(bot.getId());
  }

  @GetMapping("/{id}")
  public String botDetail(@PathVariable Long id, Authentication authentication, Model model) {
    User currentUser =
        isAuthenticated(authentication) ? userService.getCurrentUser(authentication) : null;
    Bot bot = botService.getUserBot(Optional.ofNullable(currentUser), id);

    boolean canManage = botService.canManageBot(currentUser, bot);

    byte[] pieChartBytes =
        chartService.generateResultsPieChart(bot.getWins(), bot.getLosses(), bot.getDraws());
    String base64PieChart = Base64.getEncoder().encodeToString(pieChartBytes);

    byte[] eloChartBytes = chartService.generateEloLineChart(bot.getEloHistory());
    String base64EloChart = Base64.getEncoder().encodeToString(eloChartBytes);

    int total = bot.getWins() + bot.getLosses() + bot.getDraws();
    double winRate = (total > 0) ? (bot.getWins() * 100.0 / total) : 0;
    String username = resolveOwnerUsername(bot);

    int startElo = bot.getEloHistory().isEmpty() ? bot.getElo() : bot.getEloHistory().get(0);
    int currentElo = bot.getElo();
    int trend = currentElo - startElo;

    model.addAttribute("rankingPosition", botService.findRankingPositionById(bot.getId()));
    model.addAttribute("bot", bot);
    model.addAttribute("username", username);
    model.addAttribute("initial", resolveInitial(bot));
    model.addAttribute("totalMatches", total);
    model.addAttribute("winRateFormatted", String.format("%.1f", winRate));

    model.addAttribute("pieChart", base64PieChart);
    model.addAttribute("eloChart", base64EloChart);

    model.addAttribute("startElo", startElo);
    model.addAttribute("eloTrend", (trend >= 0 ? "+" : "") + trend);
    model.addAttribute("trendClass", trend >= 0 ? "bg-success" : "bg-danger");

    model.addAttribute("canManage", canManage);
    model.addAttribute("showCode", bot.isPublic() || canManage);

    return "bot-detail";
  }

  @PutMapping("/{id}")
  public String updateBot(
      @PathVariable Long id,
      @RequestParam String name,
      @RequestParam(required = false) String description,
      @RequestParam(required = false) String code,
      @RequestParam(required = false) MultipartFile image,
      @RequestParam(required = false) String tags,
      @RequestParam boolean isPublic,
      Authentication authentication) {

    User currentUser = userService.getCurrentUser(authentication);

    botService.updateBot(currentUser, id, name, description, code, image, tags, isPublic);

    return redirectToBotDetail(id);
  }

  @DeleteMapping("/{id}")
  public String deleteBot(@PathVariable Long id, Authentication authentication) {
    User currentUser = userService.getCurrentUser(authentication);

    botService.deleteBot(currentUser, id);

    if (userService.isAdmin(currentUser)) {
      return redirectToAdminBots();
    }
    return redirectToMyBots();
  }

  @GetMapping("/create")
  public String createBot(Authentication authentication) {
    return "bot-create";
  }

  @GetMapping("/{botId}/edit")
  public String editBot(@PathVariable Long botId, Authentication authentication, Model model) {
    User currentUser = userService.getCurrentUser(authentication);
    Bot bot = botService.getEditableBotOrThrow(botId, currentUser);

    model.addAttribute("initial", resolveInitial(bot));
    model.addAttribute("bot", bot);
    return "bot-edit";
  }

  @GetMapping("/my-bots")
  public String myBotsPage(
      @PageableDefault(size = 10) Pageable pageable, Authentication authentication, Model model) {
    User currentUser = userService.getCurrentUser(authentication);

    int bestElo = botService.getMaxEloForUser(currentUser.getId());
    int totalBots = botService.getTotalBotsForUser(currentUser.getId());
    Long latestBotId = botService.getLatestBotIdForUser(currentUser.getId());

    model.addAttribute("latestBotId", latestBotId);
    model.addAttribute("hasLatestBot", latestBotId != null);
    model.addAttribute("totalBots", totalBots);
    model.addAttribute("bestElo", bestElo);
    model.addAttribute("username", currentUser.getUsername());
    model.addAttribute("size", Math.max(pageable.getPageSize(), 1));
    model.addAttribute("fromItem", 0);
    model.addAttribute("toItem", 0);
    model.addAttribute("totalElements", 0);
    return "my-bots";
  }

  @GetMapping("/my-bots/page")
  public String myBotsChunk(
      @PageableDefault(size = 5) Pageable pageable, Authentication authentication, Model model) {

    User currentUser = userService.getCurrentUser(authentication);

    Page<Bot> botPage =
        botService.getUserBots(Optional.of(currentUser.getId()), currentUser.getId(), pageable);

    int fromItem = botPage.isEmpty() ? 0 : (int) botPage.getPageable().getOffset() + 1;
    int toItem = botPage.isEmpty() ? 0 : fromItem + botPage.getNumberOfElements() - 1;

    model.addAttribute("bots", botPage.getContent());
    model.addAttribute("showEmpty", pageable.getPageNumber() == 0 && botPage.isEmpty());
    model.addAttribute("canManage", true);
    model.addAttribute("nextPage", botPage.getNumber() + 1);
    model.addAttribute("hasMore", botPage.hasNext());
    model.addAttribute("totalElements", botPage.getTotalElements());
    model.addAttribute("fromItem", fromItem);
    model.addAttribute("toItem", toItem);
    return "components/bot-page-chunk";
  }

  private boolean isAuthenticated(Authentication authentication) {
    return authentication != null
        && authentication.isAuthenticated()
        && !(authentication instanceof AnonymousAuthenticationToken);
  }

  private char resolveInitial(Bot bot) {
    if (bot == null || bot.getName() == null || bot.getName().isBlank()) {
      return '?';
    }
    return bot.getName().charAt(0);
  }

  private String resolveOwnerUsername(Bot bot) {
    if (bot == null || bot.getOwnerId() == null) {
      return "Unknown";
    }
    try {
      String username = userService.getUserById(bot.getOwnerId()).getUsername();
      return (username == null || username.isBlank()) ? "Unknown" : username;
    } catch (IllegalArgumentException ex) {
      return "Unknown";
    }
  }

  private String redirectToBotDetail(Long botId) {
    return "redirect:/bots/" + botId;
  }

  private String redirectToBotEdit(Long botId) {
    return "redirect:/bots/" + botId + "/edit";
  }

  private String redirectToMyBots() {
    return "redirect:/bots/my-bots";
  }

  private String redirectToAdminBots() {
    return "redirect:/admin/bots";
  }
}
