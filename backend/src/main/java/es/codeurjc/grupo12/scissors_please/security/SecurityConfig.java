package es.codeurjc.grupo12.scissors_please.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
  private static final String[] NON_USER_ROUTES = {
    "/",
    "/home",
    "/login",
    "/sign-up",
    "/register",
    "/css/**",
    "/js/**",
    "/images/**",
    "/tournament-images/**",
    "/bot-images/**",
    "/user-images/**",
    "/h2-console/**",
    "/matches/list",
    "/matches/list/page",
    "/matches/stats",
    "/tournaments",
    "/tournaments/page",
    "/tournaments/detail/**",
    "/tournaments/results"
  };

  private static final String[] USER_ROUTES = {"/user/profile"};

  private static final String[] USER_AND_NOT_ADMIN_ROUTES = {
    "/bots/user-bots", "/bots/user-bots/page", "/bots/create", "/bots"
  };

  private static final String[] USER_OR_ADMIN_BOT_ROUTES = {"/bots/*", "/bots/*/edit"};

  private static final String[] ADMIN_ROUTES = {"/admin/**", "/api/admin/**"};

  @Autowired private CustomOAuth2UserService customOAuth2UserService;
  @Autowired private LoginFailureHandler loginFailureHandler;

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  SessionRegistry sessionRegistry() {
    return new SessionRegistryImpl();
  }

  @Bean
  HttpSessionEventPublisher httpSessionEventPublisher() {
    return new HttpSessionEventPublisher();
  }

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
        .authorizeHttpRequests(
            authz ->
                authz
                    .requestMatchers(NON_USER_ROUTES)
                    .permitAll()
                    .requestMatchers(ADMIN_ROUTES)
                    .hasRole("ADMIN")
                    .requestMatchers(USER_AND_NOT_ADMIN_ROUTES)
                    .access(
                        new WebExpressionAuthorizationManager(
                            "hasRole('USER') and !hasRole('ADMIN')"))
                    .requestMatchers(HttpMethod.GET, "/bots/*")
                    .permitAll()
                    .requestMatchers(USER_OR_ADMIN_BOT_ROUTES)
                    .hasAnyRole("USER", "ADMIN")
                    .requestMatchers(USER_ROUTES)
                    .hasRole("USER")
                    .anyRequest()
                    .authenticated())
        .formLogin(
            form ->
                form.loginPage("/login")
                    .loginProcessingUrl("/login")
                    .defaultSuccessUrl("/home", true)
                    .failureHandler(loginFailureHandler)
                    .permitAll())
        .oauth2Login(
            oauth2 ->
                oauth2
                    .loginPage("/login")
                    .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                    .defaultSuccessUrl("/home", true)
                    .failureHandler(loginFailureHandler))
        .sessionManagement(
            session ->
                session
                    .maximumSessions(-1)
                    .sessionRegistry(sessionRegistry())
                    .expiredUrl("/login?blocked"))
        .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/").permitAll())
        .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));

    return http.build();
  }
}
