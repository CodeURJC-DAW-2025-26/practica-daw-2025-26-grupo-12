package es.codeurjc.grupo12.scissors_please.controller.api.v1.auth;

import es.codeurjc.grupo12.scissors_please.config.ResponseConstants;
import es.codeurjc.grupo12.scissors_please.dto.ExceptionResponseDto;
import es.codeurjc.grupo12.scissors_please.security.jwt.AuthResponse;
import es.codeurjc.grupo12.scissors_please.security.jwt.AuthResponse.Status;
import es.codeurjc.grupo12.scissors_please.security.jwt.LoginRequest;
import es.codeurjc.grupo12.scissors_please.security.jwt.UserLoginService;
import es.codeurjc.grupo12.scissors_please.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "JWT authentication endpoints for the REST API")
@RestController("apiAuthController")
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final UserLoginService userLoginService;
  private final UserService userService;

  public AuthController(UserLoginService userLoginService, UserService userService) {
    this.userLoginService = userLoginService;
    this.userService = userService;
  }

  @Operation(
      summary = "Log in with username and password",
      description =
          "Authenticates the user credentials and returns the JWT response. On success, the login "
              + "service also writes the authentication cookies to the HTTP response.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Authentication succeeded",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Invalid credentials",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ExceptionResponseDto.class)))
      })
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      required = true,
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = LoginRequest.class)))
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

  @Operation(
      summary = "Register a new user",
      description =
          "Creates a new user account with the provided username, email and password, then "
              + "returns the authentication response and cookies for immediate login.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Registration succeeded",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid registration payload or business validation error",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ExceptionResponseDto.class))),
        @ApiResponse(
            responseCode = "404",
            description = "User could not be authenticated after registration",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ExceptionResponseDto.class)))
      })
  @io.swagger.v3.oas.annotations.parameters.RequestBody(
      required = true,
      content =
          @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = RegisterRequest.class)))
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

  @Operation(
      summary = "Refresh the JWT session",
      description =
          "Uses the refresh-token cookie to renew the session and returns a fresh authentication "
              + "response.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Token refreshed successfully",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AuthResponse.class))),
        @ApiResponse(
            responseCode = "401",
            description = "Missing or invalid refresh token",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AuthResponse.class)))
      })
  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refreshToken(
      @Parameter(
              name = "RefreshToken",
              description = "Refresh token cookie used to renew the session",
              in = ParameterIn.COOKIE,
              required = false)
          @CookieValue(name = "RefreshToken", required = false)
          String refreshToken,
      HttpServletResponse response) {

    return userLoginService.refresh(response, refreshToken);
  }

  @Operation(
      summary = "Log out",
      description = "Invalidates the current session cookies and returns a logout response.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Logout succeeded",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AuthResponse.class)))
      })
  @PostMapping("/logout")
  public ResponseEntity<AuthResponse> logOut(HttpServletResponse response) {
    return ResponseEntity.ok(new AuthResponse(Status.SUCCESS, userLoginService.logout(response)));
  }

  private record RegisterRequest(
      @Schema(description = "Unique username for the new account", example = "jane.doe")
          String username,
      @Schema(description = "Password for the new account", example = "StrongPass123!")
          String password,
      @Schema(description = "Email address for the new account", example = "jane@example.com")
          String email) {}
}
