package es.codeurjc.grupo12.scissors_please.controller.web;

import es.codeurjc.grupo12.scissors_please.model.Bot;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.service.BotService;
import es.codeurjc.grupo12.scissors_please.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MainController {

  private final BotService botService;
  private final UserService userService;

  @GetMapping("/")
  public String index() {
    return "index";
  }

  @GetMapping("/home")
  public String home(Authentication authentication, Model model) {
    User currentUser = userService.getCurrentUser(authentication);
    List<Bot> topBots = botService.getTopBotsForUser(currentUser, true, 3);
    model.addAttribute("topBots", topBots);
    model.addAttribute("hasBots", !topBots.isEmpty());
    return "home-auth";
  }
}
