package es.codeurjc.grupo12.scissors_please.dto;

import es.codeurjc.grupo12.scissors_please.model.User;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
  private boolean success;
  private String message;
  private User user;
  private List<String> errors;

  public AuthResponse(boolean success, String message) {
    this.success = success;
    this.message = message;
  }

  public AuthResponse(boolean success, String message, User user) {
    this.success = success;
    this.message = message;
    this.user = user;
  }

  public AuthResponse(boolean success, String message, List<String> errors) {
    this.success = success;
    this.message = message;
    this.errors = errors;
  }
}
