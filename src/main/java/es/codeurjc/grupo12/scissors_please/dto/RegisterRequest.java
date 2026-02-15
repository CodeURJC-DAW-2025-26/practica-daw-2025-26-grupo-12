package es.codeurjc.grupo12.scissors_please.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
  private String username;
  private String email;
  private String password;
  private String confirmPassword;
}
