package es.codeurjc.grupo12.scissors_please.controller.web;

import es.codeurjc.grupo12.scissors_please.model.Bot;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.service.BotService;
import es.codeurjc.grupo12.scissors_please.service.UserService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/bots")
@RequiredArgsConstructor
public class BotController {

  private final BotService botService;
  private final UserService userService;

  @GetMapping("/my-bots")
  public String myBotsPage(
      @RequestParam(required = false) String user,
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
    return "my-bots";
  }

  @GetMapping("/my-bots/rows")
  public String myBotsRows(
      @RequestParam(required = false) String user,
      @RequestParam(defaultValue = "0") int from,
      @RequestParam(defaultValue = "10") int to,
      Authentication authentication,
      Model model) {
    User currentUser = userService.getCurrentUser(authentication);
    User targetUser = resolveTargetUser(user, currentUser);
    boolean showPrivate = canViewPrivate(currentUser, targetUser);
    List<Bot> bots = botService.getBotsWithinRange(targetUser, showPrivate, from, to);
    model.addAttribute("bots", bots);
    model.addAttribute("showEmpty", from == 0 && bots.isEmpty());
    return "components/bot-rows";
  }

  @GetMapping("/create")
  public String createBot() {
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
    Bot bot = new Bot();
    bot.setName(name);
    bot.setDescription(description);
    bot.setLanguage(language);
    bot.setCode(code);
    bot.setImage(image);
    bot.setPublic(isPublic);
    bot.setTags(parseTags(tags));
    botService.createBot(bot, currentUser);
    return "redirect:/bots/my-bots";
  }

  @GetMapping("/edit")
  public String editBot() {
    return "bot-edit";
  }

  @GetMapping("/detail")
  public String botDetail() {
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

  private boolean canViewPrivate(User currentUser, User targetUser) {
    return userService.isAdmin(currentUser)
        || targetUser.getUsername().equals(currentUser.getUsername());
  }
}
