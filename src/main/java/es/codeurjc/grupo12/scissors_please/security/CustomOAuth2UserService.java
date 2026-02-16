package es.codeurjc.grupo12.scissors_please.security;

import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final UserRepository userRepository;

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oauth2User = super.loadUser(userRequest);

    String provider = userRequest.getClientRegistration().getRegistrationId();
    String email = oauth2User.getAttribute("email");
    String name = oauth2User.getAttribute("name");

    log.info("OAuth2 login attempt - Provider: {}, Email: {}, Name: {}", provider, email, name);

    if (email == null) {
      log.error("Email not found in OAuth2 user attributes");
      throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
    }

    User user =
        userRepository
            .findByEmail(email)
            .orElseGet(
                () -> {
                  log.info("Creating new user from OAuth2 provider: {}", email);
                  User newUser = new User();
                  newUser.setEmail(email);
                  newUser.setUsername(name != null ? name : email.split("@")[0]);
                  newUser.setOauthProvider(provider);
                  newUser.setPassword(null);
                  newUser.setRoles(List.of("USER"));
                  return userRepository.save(newUser);
                });

    if (user.getOauthProvider() == null || !user.getOauthProvider().equals(provider)) {
      log.info("Updating OAuth provider for user: {} to {}", email, provider);
      user.setOauthProvider(provider);
      userRepository.save(user);
    }

    log.info("OAuth2 user authenticated successfully: {}", email);
    return oauth2User;
  }
}
