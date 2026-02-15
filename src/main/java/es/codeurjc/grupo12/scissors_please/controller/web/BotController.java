package es.codeurjc.grupo12.scissors_please.controller.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/bots")
public class BotController {

  @GetMapping("/my-bots")
  public String myBots() {
    return "my-bots";
  }

  @GetMapping("/create")
  public String createBot() {
    return "bot-create";
  }

  @GetMapping("/edit")
  public String editBot() {
    return "bot-edit";
  }

  @GetMapping("/detail")
  public String botDetail() {
    return "bot-detail";
  }
}
