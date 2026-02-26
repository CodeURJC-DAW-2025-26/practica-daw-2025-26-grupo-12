package es.codeurjc.grupo12.scissors_please.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

  private final CustomOAuth2UserService customOAuth2UserService;

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
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
                        "/h2-console/**")
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
                    .failureUrl("/login?error")
                    .permitAll())
        .oauth2Login(
            oauth2 ->
                oauth2
                    .loginPage("/login")
                    .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                    .defaultSuccessUrl("/home", true)
                    .failureUrl("/login?error"))
        .logout(logout -> logout.logoutUrl("/logout").logoutSuccessUrl("/").permitAll())
        .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));

    return http.build();
  }
}
