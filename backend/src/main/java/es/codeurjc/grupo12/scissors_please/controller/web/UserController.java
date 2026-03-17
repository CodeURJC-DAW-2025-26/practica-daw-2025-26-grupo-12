package es.codeurjc.grupo12.scissors_please.controller.web;

import es.codeurjc.grupo12.scissors_please.service.user.UserWebHandlerService;
import es.codeurjc.grupo12.scissors_please.views.WebFlowView;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/user")
public class UserController {

  @Autowired private UserWebHandlerService userWebHandlerService;

  @GetMapping("/profile")
  public String userProfile(
      @RequestParam(required = false) String user, Authentication authentication, Model model) {
    WebFlowView view = userWebHandlerService.userProfileHandler(user, authentication);
    view.toModel(model);
    return view.viewName();
  }

  @PostMapping("/profile/update-photo")
  public String updatePhoto(
      @RequestParam MultipartFile image, Model model, Authentication authentication)
      throws IOException {
    WebFlowView view = userWebHandlerService.updatePhotoHandler(image, authentication);
    view.toModel(model);
    return view.viewName();
  }
}
