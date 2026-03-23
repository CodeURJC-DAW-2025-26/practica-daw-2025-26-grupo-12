package es.codeurjc.grupo12.scissors_please.controller.api.v1.notifications;

import es.codeurjc.grupo12.scissors_please.service.notification.NotificationService;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController("apiNotificationControllerV1")
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationRestController {

  private final NotificationService notificationService;

  @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter subscribe(Principal principal) {
    if (principal == null) {
      throw new IllegalArgumentException(
          "User must be authenticated to subscribe to notifications");
    }
    return notificationService.subscribe(principal.getName());
  }
}
