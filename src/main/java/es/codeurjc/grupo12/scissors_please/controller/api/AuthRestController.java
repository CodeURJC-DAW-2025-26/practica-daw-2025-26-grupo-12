package es.codeurjc.grupo12.scissors_please.controller.api;

import es.codeurjc.grupo12.scissors_please.dto.AuthResponse;
import es.codeurjc.grupo12.scissors_please.dto.LoginRequest;
import es.codeurjc.grupo12.scissors_please.dto.RegisterRequest;
import es.codeurjc.grupo12.scissors_please.model.User;
import es.codeurjc.grupo12.scissors_please.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthRestController {
  @Autowired private UserService userService;

  @Autowired private AuthenticationManager authenticationManager;

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
    try {
      if (request.getPassword() == null
          || !request.getPassword().equals(request.getConfirmPassword())) {
        log.warn(
            "Registration attempt with mismatched passwords for email: {}", request.getEmail());
        return ResponseEntity.badRequest().body(new AuthResponse(false, "Passwords do not match"));
      }

      User user =
          userService.registerUser(
              request.getUsername(), request.getEmail(), request.getPassword());

      return ResponseEntity.status(HttpStatus.CREATED)
          .body(new AuthResponse(true, "User registered successfully", user));
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(new AuthResponse(false, e.getMessage()));
    } catch (Exception e) {
      log.error("Unexpected error during registration", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(new AuthResponse(false, "Registration failed"));
    }
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
    try {
      if (request.getUsername() == null || request.getPassword() == null) {
        return ResponseEntity.badRequest()
            .body(new AuthResponse(false, "Username and password are required"));
      }

      Authentication authentication =
          authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(
                  request.getUsername(), request.getPassword()));

      SecurityContextHolder.getContext().setAuthentication(authentication);

      User user =
          userService
              .findByUsername(request.getUsername())
              .orElseThrow(() -> new IllegalArgumentException("User not found"));

      log.info("User logged in successfully: {}", request.getUsername());
      return ResponseEntity.ok(new AuthResponse(true, "Login successful", user));
    } catch (BadCredentialsException e) {
      log.warn("Login attempt with invalid credentials for user: {}", request.getUsername());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new AuthResponse(false, "Invalid username or password"));
    } catch (Exception e) {
      log.error("Authentication error", e);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(new AuthResponse(false, "Authentication failed"));
    }
  }

  @PostMapping("/logout")
  public ResponseEntity<AuthResponse> logout() {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    SecurityContextHolder.clearContext();
    log.info("User logged out: {}", username);
    return ResponseEntity.ok(new AuthResponse(true, "Logout successful"));
  }

  @GetMapping("/me")
  public ResponseEntity<AuthResponse> getCurrentUser() {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();

    return userService
        .findByUsername(username)
        .map(user -> ResponseEntity.ok(new AuthResponse(true, "Current user", user)))
        .orElse(
            ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new AuthResponse(false, "Not authenticated")));
  }
}
