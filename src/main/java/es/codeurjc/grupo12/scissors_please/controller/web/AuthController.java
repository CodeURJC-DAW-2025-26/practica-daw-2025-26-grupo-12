package es.codeurjc.grupo12.scissors_please.controller.web;

import es.codeurjc.grupo12.scissors_please.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
  @Autowired private UserService userService;

  @GetMapping("/login")
  public String loginPage(
      @RequestParam(required = false) String error,
      @RequestParam(required = false) String success,
      Model model) {
    if (error != null) {
      model.addAttribute("error", true);
    }
    if (success != null) {
      model.addAttribute("success", true);
    }
    return "login";
  }

  @PostMapping("/login")
  public String login() {
    return "redirect:/home";
  }

  @GetMapping("/sign-up")
  public String signupPage() {
    return "sign-up";
  }

  @PostMapping("/register")
  public String register(
      @RequestParam String username,
      @RequestParam String email,
      @RequestParam String password,
      Model model) {
    try {
      userService.registerUser(username, email, password);
      return "redirect:/login?success";
    } catch (Exception e) {
      model.addAttribute("error", e.getMessage());
      return "sign-up";
    }
  }
}
