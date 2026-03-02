package es.codeurjc.grupo12.scissors_please.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
      throws IOException, ServletException {
    if (isBlockedException(exception)) {
      getRedirectStrategy().sendRedirect(request, response, "/login?blocked");
      return;
    }
    getRedirectStrategy().sendRedirect(request, response, "/login?error");
  }

  private boolean isBlockedException(AuthenticationException exception) {
    if (exception instanceof DisabledException) {
      return true;
    }
    return exception.getMessage() != null
        && exception.getMessage().toLowerCase().contains("blocked");
  }
}
