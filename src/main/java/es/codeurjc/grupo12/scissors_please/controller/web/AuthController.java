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
      @RequestParam(required = false) String blocked,
      @RequestParam(required = false) String success,
      Model model) {
    if (blocked != null) {
      model.addAttribute("errorMessage", "Your account is blocked. Contact an administrator.");
      return "login";
    }
    if (error != null) {
      model.addAttribute("errorMessage", "Invalid username or password");
    }
    if (success != null) {
      model.addAttribute("successMessage", "Account created successfully! You can now log in.");
    }
    return "login";
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
      @RequestParam String confirmPassword,
      Model model) {
    try {
      if (!password.equals(confirmPassword)) {
        model.addAttribute("errorMessage", "Passwords do not match");
        return "sign-up";
      }
      userService.registerUser(username, email, password);
      return "redirect:/login?success";
    } catch (Exception e) {
      model.addAttribute("errorMessage", e.getMessage());
      return "sign-up";
    }
  }
}
