package es.codeurjc.grupo12.scissors_please.controller.api.v1.auth;

import es.codeurjc.grupo12.scissors_please.config.ResponseConstants;
import es.codeurjc.grupo12.scissors_please.dto.ResponseDto;
import es.codeurjc.grupo12.scissors_please.security.jwt.AuthResponse;
import es.codeurjc.grupo12.scissors_please.security.jwt.AuthResponse.Status;
import es.codeurjc.grupo12.scissors_please.security.jwt.LoginRequest;
import es.codeurjc.grupo12.scissors_please.security.jwt.UserLoginService;
import es.codeurjc.grupo12.scissors_please.service.user.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("apiAuthController")
@RequestMapping("/api/v1/auth")
public class AuthController {

  @Autowired private UserLoginService userLoginService;
  @Autowired private UserService userService;

  @PostMapping("/login")
  public ResponseDto login(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
    if (userService.authenticate(loginRequest.getUsername(), loginRequest.getPassword())) {
      return new ResponseDto(
          false,
          ResponseConstants.OK_CODE_INT,
          ResponseConstants.OK,
          userLoginService.login(response, loginRequest));
    }
    return new ResponseDto(
        true, ResponseConstants.NOT_FOUND_CODE_INT, ResponseConstants.ELEMENT_NOT_FOUND, null);
  }

  @PostMapping("/register")
  public ResponseDto register(@RequestBody RegisterRequest request, HttpServletResponse response) {
    try {
      userService.registerUser(request.username(), request.email(), request.password());
    } catch (IllegalArgumentException error) {
      return new ResponseDto(
          true, ResponseConstants.BAD_REQUEST_CODE_INT, error.getMessage(), null);
    }

    LoginRequest loginRequest = new LoginRequest(request.username(), request.password());
    if (userService.authenticate(loginRequest.getUsername(), loginRequest.getPassword())) {
      return new ResponseDto(
          false,
          ResponseConstants.OK_CODE_INT,
          ResponseConstants.OK,
          userLoginService.login(response, loginRequest));
    }
    return new ResponseDto(
        true, ResponseConstants.NOT_FOUND_CODE_INT, ResponseConstants.ELEMENT_NOT_FOUND, null);
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
