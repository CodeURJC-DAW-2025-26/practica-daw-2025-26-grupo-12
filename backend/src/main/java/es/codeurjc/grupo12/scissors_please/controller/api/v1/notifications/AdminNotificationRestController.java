package es.codeurjc.grupo12.scissors_please.controller.api.v1.notifications;

import es.codeurjc.grupo12.scissors_please.service.notification.NotificationService;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("apiAdminNotificationControllerV1")
@RequestMapping("/api/v1/notifications/admin")
@RequiredArgsConstructor
public class AdminNotificationRestController {

  private final NotificationService notificationService;

  @PostMapping
  public ResponseEntity<Void> sendNotification(@RequestBody NotificationRequest request) {
    notificationService.createAndSendNotification(request.getUsernames(), request.getMessage());
    return ResponseEntity.ok().build();
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class NotificationRequest {
    private List<String> usernames;
    private String message;
  }
}
