package es.codeurjc.grupo12.scissors_please.controller.api.v1.auth;

import es.codeurjc.grupo12.scissors_please.config.ResponseConstants;
import es.codeurjc.grupo12.scissors_please.dto.ExceptionResponseDto;
import es.codeurjc.grupo12.scissors_please.security.jwt.AuthResponse;
import es.codeurjc.grupo12.scissors_please.security.jwt.AuthResponse.Status;
import es.codeurjc.grupo12.scissors_please.security.jwt.LoginRequest;
import es.codeurjc.grupo12.scissors_please.security.jwt.UserLoginService;
import es.codeurjc.grupo12.scissors_please.service.user.UserService;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("apiAuthController")
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final UserLoginService userLoginService;
  private final UserService userService;

  public AuthController(UserLoginService userLoginService, UserService userService) {
    this.userLoginService = userLoginService;
    this.userService = userService;
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(
      @RequestBody LoginRequest loginRequest, HttpServletResponse response) {
    if (userService.authenticate(loginRequest.getUsername(), loginRequest.getPassword())) {
      return userLoginService.login(response, loginRequest);
    }
    ExceptionResponseDto error =
        new ExceptionResponseDto(ResponseConstants.ELEMENT_NOT_FOUND, LocalDateTime.now());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @PostMapping("/register")
  public ResponseEntity<?> register(
      @RequestBody RegisterRequest request, HttpServletResponse response) {
    try {
      userService.registerUser(request.username(), request.email(), request.password());
    } catch (IllegalArgumentException e) {
      ExceptionResponseDto error = new ExceptionResponseDto(e.getMessage(), LocalDateTime.now());

      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    LoginRequest loginRequest = new LoginRequest(request.username(), request.password());
    if (userService.authenticate(loginRequest.getUsername(), loginRequest.getPassword())) {
      return userLoginService.login(response, loginRequest);
    }

    ExceptionResponseDto error =
        new ExceptionResponseDto(ResponseConstants.ELEMENT_NOT_FOUND, LocalDateTime.now());

    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refreshToken(
      @CookieValue(name = "RefreshToken", required = false) String refreshToken,
      HttpServletResponse response) {

    return userLoginService.refresh(response, refreshToken);
  }

  @PostMapping("/logout")
  public ResponseEntity<AuthResponse> logOut(HttpServletResponse response) {
    return ResponseEntity.ok(new AuthResponse(Status.SUCCESS, userLoginService.logout(response)));
  }

  private record RegisterRequest(String username, String password, String email) {}
}
