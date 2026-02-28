package es.codeurjc.grupo12.scissors_please.security;

import es.codeurjc.grupo12.scissors_please.model.User;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class ActiveSessionService {

  @Autowired private SessionRegistry sessionRegistry;

  public void expireSessions(User user) {
    List<Object> principals = sessionRegistry.getAllPrincipals();
    for (Object principal : principals) {
      if (!belongsToUser(principal, user)) {
        continue;
      }
      for (SessionInformation session : sessionRegistry.getAllSessions(principal, false)) {
        session.expireNow();
      }
    }
  }

  private boolean belongsToUser(Object principal, User user) {
    if (principal instanceof UserDetails userDetails) {
      return user.getUsername().equalsIgnoreCase(userDetails.getUsername());
    }
    if (principal instanceof OAuth2User oauth2User) {
      String email = oauth2User.getAttribute("email");
      String oauthName = oauth2User.getName();
      return user.getEmail().equalsIgnoreCase(email)
          || user.getUsername().equalsIgnoreCase(oauthName);
    }
    if (principal instanceof String principalName) {
      return user.getUsername().equalsIgnoreCase(principalName);
    }
    return false;
  }
}
