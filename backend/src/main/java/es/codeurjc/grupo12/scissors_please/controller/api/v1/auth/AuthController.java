package es.codeurjc.grupo12.scissors_please.controller.api.v1.auth;

import es.codeurjc.grupo12.scissors_please.config.ResponseConstants;
import es.codeurjc.grupo12.scissors_please.dto.ExceptionResponseDto;
import es.codeurjc.grupo12.scissors_please.dto.auth.AdminStatusResponse;
import es.codeurjc.grupo12.scissors_please.dto.auth.RegisterRequest;
import es.codeurjc.grupo12.scissors_please.security.jwt.AuthResponse;
import es.codeurjc.grupo12.scissors_please.security.jwt.AuthResponse.Status;
import es.codeurjc.grupo12.scissors_please.security.jwt.JwtTokenProvider;
import es.codeurjc.grupo12.scissors_please.security.jwt.LoginRequest;
import es.codeurjc.grupo12.scissors_please.security.jwt.TokenType;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Authentication", description = "JWT authentication endpoints for the REST API")
@RestController("apiAuthController")
@RequestMapping("/api/v1/auth")
public class AuthController {

  private static final String ADMIN_AUTHORITY = "ROLE_ADMIN";
  private static final String ADMIN_ROLE = "ADMIN";

  private final UserLoginService userLoginService;
  private final UserService userService;
  private final JwtTokenProvider jwtTokenProvider;
  private final UserDetailsService userDetailsService;

  public AuthController(
      UserLoginService userLoginService,
      UserService userService,
      JwtTokenProvider jwtTokenProvider,
      UserDetailsService userDetailsService) {
    this.userLoginService = userLoginService;
    this.userService = userService;
    this.jwtTokenProvider = jwtTokenProvider;
    this.userDetailsService = userDetailsService;
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
      summary = "Check admin role",
      description = "Returns whether the current authenticated user has the ADMIN role.")
  @ApiResponse(
      responseCode = "200",
      description = "Admin status returned",
      content = @Content(schema = @Schema(implementation = AdminStatusResponse.class)))
  @GetMapping("/is-admin")
  public ResponseEntity<AdminStatusResponse> isAdmin(
      Authentication authentication,
      @CookieValue(name = "RefreshToken", required = false) String refreshToken) {
    boolean admin = hasAdminRole(authentication) || hasAdminRefreshToken(refreshToken);
    return ResponseEntity.ok(new AdminStatusResponse(admin));
  }

  private boolean hasAdminRole(Authentication authentication) {
    return authentication != null
        && authentication.isAuthenticated()
        && authentication.getAuthorities().stream()
            .anyMatch(
                authority ->
                    ADMIN_AUTHORITY.equals(authority.getAuthority())
                        || ADMIN_ROLE.equals(authority.getAuthority()));
  }

  private boolean hasAdminRefreshToken(String refreshToken) {
    if (refreshToken == null || refreshToken.isBlank()) {
      return false;
    }

    try {
      var claims = jwtTokenProvider.validateToken(refreshToken);
      if (!TokenType.REFRESH.name().equals(claims.get("type", String.class))) {
        return false;
      }

      UserDetails userDetails = userDetailsService.loadUserByUsername(claims.getSubject());
      return userDetails.getAuthorities().stream()
          .anyMatch(
              authority ->
                  ADMIN_AUTHORITY.equals(authority.getAuthority())
                      || ADMIN_ROLE.equals(authority.getAuthority()));
    } catch (Exception exception) {
      return false;
    }
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
}
