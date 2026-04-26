package es.codeurjc.grupo12.scissors_please.controller.web;

import es.codeurjc.grupo12.scissors_please.service.bot.BotWebHandlerService;
import es.codeurjc.grupo12.scissors_please.views.WebFlowView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/bots")
public class BotController {

  @Autowired private BotWebHandlerService botWebHandlerService;

  @PostMapping
  public String createBot(
      @RequestParam String name,
      @RequestParam(required = false) String description,
      @RequestParam(required = false) String code,
      @RequestParam(required = false) String tags,
      @RequestParam(required = false) MultipartFile image,
      @RequestParam(defaultValue = "false") boolean isPublic,
      Authentication authentication) {
    return botWebHandlerService
        .createBotHandler(name, description, code,tags, image, isPublic, authentication)
        .viewName();
  }

  @GetMapping("/{id}")
  public String botDetail(@PathVariable Long id, Authentication authentication, Model model) {
    WebFlowView view = botWebHandlerService.botDetailHandler(id, authentication);
    view.toModel(model);
    return view.viewName();
  }

  @PutMapping("/{id}")
  public String updateBot(
      @PathVariable Long id,
      @RequestParam String name,
      @RequestParam(required = false) String description,
      @RequestParam(required = false) String code,
      @RequestParam(required = false) MultipartFile image,
      @RequestParam(required = false) String tags,
      @RequestParam boolean isPublic,
      Authentication authentication) {
    return botWebHandlerService
        .updateBotHandler(id, name, description, code, image, tags, isPublic, authentication)
        .viewName();
  }

  @DeleteMapping("/{id}")
  public String deleteBot(@PathVariable Long id, Authentication authentication) {
    return botWebHandlerService.deleteBotHandler(id, authentication).viewName();
  }

  @GetMapping("/create")
  public String createBot(Model model) {
    WebFlowView view = botWebHandlerService.createBotPageHandler();
    view.toModel(model);
    return view.viewName();
  }

  @GetMapping("/{botId}/edit")
  public String editBot(@PathVariable Long botId, Authentication authentication, Model model) {
    WebFlowView view = botWebHandlerService.editBotHandler(botId, authentication);
    view.toModel(model);
    return view.viewName();
  }

  @GetMapping("/user-bots")
  public String userBotsPage(
      @RequestParam(required = false) String username,
      @PageableDefault(size = 10) Pageable pageable,
      Authentication authentication,
      Model model) {
    WebFlowView view = botWebHandlerService.userBotsPageHandler(username, pageable, authentication);
    view.toModel(model);
    return view.viewName();
  }

  @GetMapping("/user-bots/page")
  public String userBotsChunk(
      @RequestParam(required = false) String username,
      @PageableDefault(size = 5) Pageable pageable,
      Authentication authentication,
      Model model) {
    WebFlowView view =
        botWebHandlerService.userBotsChunkHandler(username, pageable, authentication);
    view.toModel(model);
    return view.viewName();
  }
}
