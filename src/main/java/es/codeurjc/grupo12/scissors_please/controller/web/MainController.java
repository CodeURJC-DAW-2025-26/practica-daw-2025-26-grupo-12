package es.codeurjc.grupo12.scissors_please.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

  @GetMapping("/")
  public String index() {
    return "index";
  }

  @GetMapping("/home")
  public String home() {
    return "home-auth";
  }
}
