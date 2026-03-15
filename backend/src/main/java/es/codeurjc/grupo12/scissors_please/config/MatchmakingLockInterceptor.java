package es.codeurjc.grupo12.scissors_please.config;

import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.service.match.MatchService;
import es.codeurjc.grupo12.scissors_please.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class MatchmakingLockInterceptor implements HandlerInterceptor {

  private static final Set<String> EXACT_ALLOWED_PATHS =
      Set.of("/matches/search", "/matches/search/status", "/error");

  @Autowired private MatchService matchService;
  @Autowired private UserService userService;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    if (isAllowedRequest(request)) {
      return true;
    }

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (!isAuthenticated(authentication)) {
      return true;
    }

    User currentUser;
    try {
      currentUser = userService.getCurrentUser(authentication);
    } catch (IllegalArgumentException exception) {
      return true;
    }

    if (!matchService.hasActiveMatchmaking(currentUser.getId())) {
      return true;
    }

    response.sendRedirect("/matches/search");
    return false;
  }

  private boolean isAllowedRequest(HttpServletRequest request) {
    String path = request.getRequestURI();
    return EXACT_ALLOWED_PATHS.contains(path)
        || path.startsWith("/matches/battle")
        || path.startsWith("/matches/stats")
        || path.startsWith("/matches/cancel")
        || path.startsWith("/matches/start")
        || path.startsWith("/matches/rematch/request")
        || path.startsWith("/matches/rematch/accept");
  }

  private boolean isAuthenticated(Authentication authentication) {
    return authentication != null
        && authentication.isAuthenticated()
        && !(authentication instanceof AnonymousAuthenticationToken);
  }
}
