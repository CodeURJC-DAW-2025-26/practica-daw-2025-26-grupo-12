package es.codeurjc.grupo12.scissors_please.controller.web;

import es.codeurjc.grupo12.scissors_please.service.main.MainWebHandlerService;
import es.codeurjc.grupo12.scissors_please.views.WebFlowView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {

  @Autowired private MainWebHandlerService mainWebHandlerService;

  @GetMapping("/")
  public String index() {
    return mainWebHandlerService.indexHandler().viewName();
  }

  @GetMapping("/home")
  public String home(Authentication authentication, Model model) {
    WebFlowView view = mainWebHandlerService.homeHandler(authentication);
    view.toModel(model);
    return view.viewName();
  }
}
