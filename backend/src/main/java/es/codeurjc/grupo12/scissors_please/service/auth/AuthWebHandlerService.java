package es.codeurjc.grupo12.scissors_please.service.auth;

import es.codeurjc.grupo12.scissors_please.service.user.UserService;
import es.codeurjc.grupo12.scissors_please.views.WebFlowView;
import es.codeurjc.grupo12.scissors_please.views.WebPageView;
import es.codeurjc.grupo12.scissors_please.views.WebRedirectView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthWebHandlerService {

  @Autowired private UserService userService;

  public WebFlowView loginPageHandler(String error, String blocked, String success) {
    WebPageView view = WebPageView.of("login");
    if (blocked != null) {
      return view.attribute("errorMessage", "Your account is blocked. Contact an administrator.");
    }
    if (error != null) {
      view.attribute("errorMessage", "Invalid username or password");
    }
    if (success != null) {
      view.attribute("successMessage", "Account created successfully! You can now log in.");
    }
    return view;
  }

  public WebFlowView signupPageHandler() {
    return WebPageView.of("sign-up");
  }

  public WebFlowView registerHandler(
      String username, String email, String password, String confirmPassword) {
    if (!password.equals(confirmPassword)) {
      return WebPageView.of("sign-up").attribute("errorMessage", "Passwords do not match");
    }

    try {
      userService.registerUser(username, email, password);
      return WebRedirectView.to("/login?success");
    } catch (Exception exception) {
      return WebPageView.of("sign-up").attribute("errorMessage", exception.getMessage());
    }
  }
}
