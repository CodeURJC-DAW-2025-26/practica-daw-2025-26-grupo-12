package es.codeurjc.grupo12.scissors_please.controller.web;

import es.codeurjc.grupo12.scissors_please.service.auth.AuthWebHandlerService;
import es.codeurjc.grupo12.scissors_please.views.WebFlowView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

  @Autowired private AuthWebHandlerService authWebHandlerService;

  @GetMapping("/login")
  public String loginPage(
      @RequestParam(required = false) String error,
      @RequestParam(required = false) String blocked,
      @RequestParam(required = false) String success,
      Model model) {
    WebFlowView view = authWebHandlerService.loginPageHandler(error, blocked, success);
    view.toModel(model);
    return view.viewName();
  }

  @GetMapping("/sign-up")
  public String signupPage(Model model) {
    WebFlowView view = authWebHandlerService.signupPageHandler();
    view.toModel(model);
    return view.viewName();
  }

  @PostMapping("/register")
  public String register(
      @RequestParam String username,
      @RequestParam String email,
      @RequestParam String password,
      @RequestParam String confirmPassword,
      Model model) {
    WebFlowView view =
        authWebHandlerService.registerHandler(username, email, password, confirmPassword);
    view.toModel(model);
    return view.viewName();
  }
}
