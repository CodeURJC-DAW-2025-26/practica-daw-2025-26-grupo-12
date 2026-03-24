package es.codeurjc.grupo12.scissors_please.dto.notifications;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
  private List<String> usernames;
  private String message;
}
