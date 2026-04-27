package es.codeurjc.grupo12.scissors_please.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {
  @GetMapping({
    "/new",
    "/new/",
    "/new/{path:[^\\.]*}",
    "/new/*/{path:[^\\.]*}",
    "/new/*/*/{path:[^\\.]*}",
    "/new/*/*/*/{path:[^\\.]*}"
  })
  public String forwardSpaRoutes() {
    return "forward:/new/index.html";
  }
}
