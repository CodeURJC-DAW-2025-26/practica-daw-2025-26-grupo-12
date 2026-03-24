package es.codeurjc.grupo12.scissors_please.security.jwt;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resultado de una operacion de autenticacion")
public class AuthResponse {

  @Schema(description = "Estado de la operacion", example = "SUCCESS")
  private Status status;

  @Schema(description = "Mensaje asociado al resultado", example = "Login successful")
  private String message;

  @Schema(description = "Descripcion del error si la operacion falla", example = "Invalid credentials")
  private String error;

  @Schema(description = "Estados posibles de una respuesta de autenticacion")
  public enum Status {
    SUCCESS,
    FAILURE
  }

  public AuthResponse() {}

  public AuthResponse(Status status, String message) {
    this.status = status;
    this.message = message;
  }

  public AuthResponse(Status status, String message, String error) {
    this.status = status;
    this.message = message;
    this.error = error;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  @Override
  public String toString() {
    return "LoginResponse [status=" + status + ", message=" + message + ", error=" + error + "]";
  }
}
