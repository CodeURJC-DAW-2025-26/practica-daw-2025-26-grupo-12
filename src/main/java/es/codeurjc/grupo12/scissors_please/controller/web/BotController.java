package es.codeurjc.grupo12.scissors_please.controller.web;

import es.codeurjc.grupo12.scissors_please.model.Bot;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.service.BotService;
import es.codeurjc.grupo12.scissors_please.service.UserService;
import java.util.ArrayList;
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

@Controller
@RequestMapping("/bots")
public class BotController {

  private static final String REDIRECT_MY_BOTS = "redirect:/bots/my-bots";

  @Autowired private BotService botService;
  @Autowired private UserService userService;

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
      @RequestParam(required = false) String image,
      @RequestParam(defaultValue = "false") boolean isPublic,
      @RequestParam(required = false) String tags,
      Authentication authentication) {
    User currentUser = userService.getCurrentUser(authentication);
    if (isAdmin(currentUser)) {
      return REDIRECT_MY_BOTS;
    }
    Bot bot = new Bot();
    bot.setName(name);
    bot.setDescription(description);
    bot.setLanguage(language);
    bot.setImage(image);
    bot.setPublic(isPublic);
    bot.setTags(parseTags(tags));
    botService.createBot(bot, currentUser);
    return REDIRECT_MY_BOTS;
  }

  @PostMapping("/{id}/delete")
  public String deleteBot(@PathVariable Long id, Authentication authentication) {
    User currentUser = userService.getCurrentUser(authentication);
    Bot bot = botService.getBotById(id).orElseThrow();
    if (canManageBot(currentUser, bot)) {
      botService.deleteBot(id);
    }
    return REDIRECT_MY_BOTS;
  }

  @GetMapping("/edit/{id}")
  public String editBot(@PathVariable Long id, Model model) {
    Optional<Bot> opBot = botService.getBotById(id);
    if (opBot.isPresent()) {
      Bot bot = opBot.get();
      model.addAttribute("bot", bot);
      return "bot-edit";
    }
    return "error";
  }

  @PostMapping("/edit/{id}")
  public String editBot(
      @PathVariable Long id,
      @RequestParam String botName,
      @RequestParam(required = false) String description,
      @RequestParam(required = false) String language,
      @RequestParam(required = false) String code,
      @RequestParam(required = false) String image,
      @RequestParam boolean visibility,
      @RequestParam(required = false) String tags,
      Authentication authentication) {

    Optional<Bot> opBot = botService.getBotById(id);
    if (opBot.isPresent()) {
      Bot bot = opBot.get();
      bot.setName(botName);
      bot.setDescription(description != null ? description : "");
      bot.setLanguage(language != null ? language : "");
      bot.setCode(code != null ? code : "");
      bot.setImage(image != null ? image : "");
      bot.setPublic(visibility);
      bot.setTags(parseTags(tags));
      User currentUser = userService.getCurrentUser(authentication);
      botService.updateBot(bot, currentUser);

      return "redirect:/bots/my-bots";
    }
    return "error";
  }

  @GetMapping("/detail/{id}")
  public String botDetail(@PathVariable Long id, Model model) {
    Optional<Bot> opBot = botService.getBotById(id);
    if (opBot.isPresent()) {

      Bot bot = opBot.get();
      String username = userService.getUserById(bot.getOwnerId()).getUsername();
      model.addAttribute("bot", bot);
      model.addAttribute("username", username);
      return "bot-detail";
    }
    return "error";
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
}
