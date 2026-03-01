package es.codeurjc.grupo12.scissors_please.controller.web;

import es.codeurjc.grupo12.scissors_please.config.ErrorConstants;
import es.codeurjc.grupo12.scissors_please.model.Bot;
import es.codeurjc.grupo12.scissors_please.model.Image;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.service.BotService;
import es.codeurjc.grupo12.scissors_please.service.ChartService;
import es.codeurjc.grupo12.scissors_please.service.UserService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
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

@Controller
@RequestMapping("/bots")
public class BotController {

  private static final String REDIRECT_MY_BOTS = "redirect:/bots/my-bots";

  @Autowired private BotService botService;
  @Autowired private UserService userService;
  @Autowired private ChartService chartService;

  @GetMapping("/my-bots")
  public String myBotsPage(
      @RequestParam(required = false) String user,
      @PageableDefault(size = 5) Pageable pageable,
      Authentication authentication,
      Model model) {
    User currentUser = userService.getCurrentUser(authentication);
    User targetUser = resolveTargetUser(user, currentUser);
    boolean showPrivate = canViewPrivate(currentUser, targetUser);
    List<Bot> bots = botService.getBotsForUser(targetUser, showPrivate);
    int bestElo = bots.stream().mapToInt(Bot::getElo).max().orElse(0);
    Long latestBotId = bots.stream().map(Bot::getId).max(Long::compare).orElse(null);
    model.addAttribute("latestBotId", latestBotId);
    model.addAttribute("totalBots", bots.size());
    model.addAttribute("bestElo", bestElo);
    model.addAttribute("username", targetUser.getUsername());
    model.addAttribute("size", Math.max(pageable.getPageSize(), 1));
    model.addAttribute("fromItem", 0);
    model.addAttribute("toItem", 0);
    model.addAttribute("totalElements", 0);
    model.addAttribute(
        "userQuery",
        targetUser.getUsername().equals(currentUser.getUsername()) ? "" : targetUser.getUsername());
    return "my-bots";
  }

  @GetMapping("/my-bots/page")
  public String myBotsChunk(
      @RequestParam(required = false) String user,
      @PageableDefault(size = 5) Pageable pageable,
      Authentication authentication,
      Model model) {
    User currentUser = userService.getCurrentUser(authentication);
    User targetUser = resolveTargetUser(user, currentUser);
    boolean showPrivate = canViewPrivate(currentUser, targetUser);
    BotService.BotPage botPage = botService.getBotPage(targetUser, showPrivate, pageable);
    model.addAttribute("bots", botPage.bots());
    model.addAttribute("showEmpty", pageable.getPageNumber() == 0 && botPage.bots().isEmpty());
    model.addAttribute("canManage", showPrivate);
    model.addAttribute("nextPage", botPage.nextPage());
    model.addAttribute("hasMore", botPage.hasMore());
    model.addAttribute("totalElements", botPage.totalElements());
    model.addAttribute("fromItem", botPage.fromItem());
    model.addAttribute("toItem", botPage.toItem());
    return "components/bot-page-chunk";
  }

  @GetMapping("/create")
  public String createBot(Authentication authentication) {
    User currentUser = userService.getCurrentUser(authentication);
    if (isAdmin(currentUser)) {
      return REDIRECT_MY_BOTS;
    }
    return "bot-create";
  }

  @PostMapping("/create")
  public String createBot(
      @RequestParam String name,
      @RequestParam(required = false) String description,
      @RequestParam(required = false) String language,
      @RequestParam(required = false) String code,
      @RequestParam(defaultValue = "false") boolean isPublic,
      @RequestParam(required = false) String tags,
      @RequestParam(required = false) MultipartFile image,
      Model model,
      Authentication authentication) {
    User currentUser = userService.getCurrentUser(authentication);
    if (isAdmin(currentUser)) {
      return REDIRECT_MY_BOTS;
    }
    Bot bot = new Bot();
    bot.setName(name);
    bot.setDescription(description);
    if (!handleImageUpload(bot, image)) {
      model.addAttribute("errorMessage", ErrorConstants.IMAGE_ERROR_UPLOAD);
      return "error";
    }
    bot.setPublic(isPublic);
    bot.setTags(parseTags(tags));
    botService.createBot(bot, currentUser);
    return REDIRECT_MY_BOTS;
  }

  @PostMapping("/{id}/delete")
  public String deleteBot(@PathVariable Long id, Authentication authentication, Model model) {
    User currentUser = userService.getCurrentUser(authentication);
    Bot bot = botService.getBotById(id).orElseThrow();
    if (canManageBot(currentUser, bot)) {
      botService.deleteBot(id);
      return REDIRECT_MY_BOTS;
    }
    model.addAttribute("errorMessage", ErrorConstants.ACCESS_DENIED);
    return "error";
  }

  @GetMapping("/edit/{id}")
  public String editBot(@PathVariable Long id, Authentication authentication, Model model) {
    User currentUser = userService.getCurrentUser(authentication);
    Optional<Bot> opBot = botService.getBotById(id);
    if (opBot.isPresent()) {
      Bot bot = opBot.get();
      if (canManageBot(currentUser, bot)) {
        model.addAttribute("initial", bot.getName().charAt(0));
        model.addAttribute("bot", bot);
        return "bot-edit";
      }
      model.addAttribute("errorMessage", ErrorConstants.ACCESS_DENIED);
      return "error";
    }
    model.addAttribute("errorMessage", ErrorConstants.BOT_NOT_FOUND);
    return "error";
  }

  @PostMapping("/edit/{id}")
  public String editBot(
      @PathVariable Long id,
      @RequestParam String name,
      @RequestParam(required = false) String description,
      @RequestParam(required = false) String language,
      @RequestParam(required = false) String code,
      @RequestParam(required = false) MultipartFile image,
      @RequestParam boolean isPublic,
      @RequestParam(required = false) String tags,
      Model model,
      Authentication authentication) {

    User currentUser = userService.getCurrentUser(authentication);
    Optional<Bot> opBot = botService.getBotById(id);
    if (opBot.isPresent()) {
      Bot bot = opBot.get();
      if (!canManageBot(currentUser, bot)) {
        model.addAttribute("errorMessage", ErrorConstants.ACCESS_DENIED);
        return "error";
      }
      bot.setName(name);
      bot.setDescription(description != null ? description : "");
      bot.setCode(code != null ? code : "");
      if (!handleImageUpload(bot, image)) {
        model.addAttribute("errorMessage", ErrorConstants.IMAGE_ERROR_UPLOAD);
        return "error";
      }
      bot.setPublic(isPublic);
      bot.setTags(parseTags(tags));
      botService.updateBot(bot, currentUser);

      return "redirect:/bots/my-bots";
    }
    model.addAttribute("errorMessage", ErrorConstants.BOT_NOT_FOUND);
    return "error";
  }

  @GetMapping("/detail/{id}")
  public String botDetail(@PathVariable Long id, Authentication authentication, Model model) {
    User currentUser = null;
    if (authentication != null) {
      currentUser = userService.getCurrentUser(authentication);
    }
    Optional<Bot> opBot = botService.getBotById(id);
    if (opBot.isEmpty()) {
      model.addAttribute("errorMessage", ErrorConstants.BOT_NOT_FOUND);
      return "error";
    }
    Bot bot = opBot.get();

    boolean canManage = (currentUser != null) && canManageBot(currentUser, bot);

    byte[] pieChartBytes =
        chartService.generateResultsPieChart(bot.getWins(), bot.getLosses(), bot.getDraws());
    String base64PieChart = Base64.getEncoder().encodeToString(pieChartBytes);

    byte[] eloChartBytes = chartService.generateEloLineChart(bot.getEloHistory());
    String base64EloChart = Base64.getEncoder().encodeToString(eloChartBytes);

    int total = bot.getWins() + bot.getLosses() + bot.getDraws();
    double winRate = (total > 0) ? (bot.getWins() * 100.0 / total) : 0;
    String username = userService.getUserById(bot.getOwnerId()).getUsername();

    int startElo = bot.getEloHistory().isEmpty() ? bot.getElo() : bot.getEloHistory().get(0);
    int currentElo = bot.getElo();
    int trend = currentElo - startElo;

    model.addAttribute("totalMatches", total);
    model.addAttribute("bot", bot);
    model.addAttribute("username", username);
    model.addAttribute("initial", bot.getName().charAt(0));
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

  private List<String> parseTags(String tags) {
    if (tags == null || tags.isBlank()) {
      return new ArrayList<>();
    }
    List<String> parsedTags = new ArrayList<>();
    for (String tag : tags.split(",")) {
      String trimmedTag = tag.trim();
      if (!trimmedTag.isEmpty()) {
        parsedTags.add(trimmedTag);
      }
    }
    return parsedTags;
  }

  private User resolveTargetUser(String user, User currentUser) {
    return user == null || user.isBlank()
        ? currentUser
        : userService.findByUsername(user).orElse(currentUser);
  }

  private boolean canManageBot(User currentUser, Bot bot) {
    return isAdmin(currentUser)
        || (bot.getOwnerId() != null && bot.getOwnerId().equals(currentUser.getId()));
  }

  private boolean canViewPrivate(User currentUser, User targetUser) {
    return isAdmin(currentUser) || targetUser.getUsername().equals(currentUser.getUsername());
  }

  private boolean isAdmin(User user) {
    return userService.isAdmin(user);
  }

  private boolean handleImageUpload(Bot bot, MultipartFile imageFile) {
    if (imageFile == null || imageFile.isEmpty()) {
      return true;
    }

    String contentType = imageFile.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      return false;
    }

    try {
      Image img = new Image();
      img.setFilename(imageFile.getOriginalFilename());
      img.setContentType(contentType);
      img.setData(imageFile.getBytes());

      bot.setImage(img);
      return true;

    } catch (IOException e) {
      return false;
    }
  }
}
