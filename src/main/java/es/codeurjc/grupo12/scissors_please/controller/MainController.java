package es.codeurjc.grupo12.scissors_please.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

  @GetMapping("/")
  public String index() {
    return "index";
  }

  @GetMapping("/login")
  public String login() {
    return "login";
  }

  @GetMapping("/sign-up")
  public String signUp() {
    return "sign-up";
  }

  @GetMapping("/home")
  public String home() {
    return "home-auth";
  }
}
