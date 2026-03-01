package es.codeurjc.grupo12.scissors_please.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

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
                    .requestMatchers(
                        "/",
                        "/home",
                        "/login",
                        "/sign-up",
                        "/register",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/tournament-images/**",
                        "/h2-console/**")
                    .permitAll()
                    .requestMatchers(
                        HttpMethod.GET,
                        "/matches/list",
                        "/matches/list/page",
                        "/matches/stats",
                        "/tournaments",
                        "/tournaments/page",
                        "/tournaments/detail/**",
                        "/tournaments/results")
                    .permitAll()
                    .requestMatchers("/admin/**")
                    .hasRole("ADMIN")
                    .requestMatchers("/api/admin/**")
                    .hasRole("ADMIN")
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
