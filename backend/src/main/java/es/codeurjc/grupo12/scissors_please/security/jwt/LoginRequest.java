package es.codeurjc.grupo12.scissors_please.security.jwt;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Credenciales usadas para iniciar sesion")
public class LoginRequest {

  @Schema(description = "Nombre de usuario", example = "player1")
  private String username;

  @Schema(description = "Contrasena del usuario", example = "s3cret")
  private String password;

  public LoginRequest() {}

  public LoginRequest(String username, String password) {
    this.username = username;
    this.password = password;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @Override
  public String toString() {
    return "LoginRequest [username=" + username + ", password=" + password + "]";
  }
}
