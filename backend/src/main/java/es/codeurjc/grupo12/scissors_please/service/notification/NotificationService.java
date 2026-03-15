package es.codeurjc.grupo12.scissors_please.service.notification;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
public class NotificationService {
  private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

  public SseEmitter subscribe(String username) {
    SseEmitter emitter = new SseEmitter(60 * 60 * 1000L);
    emitters.put(username, emitter);

    log.info("User {} subscribed to notifications", username);

    emitter.onCompletion(
        () -> {
          log.info("Emitter for user {} completed", username);
          emitters.remove(username);
        });

    emitter.onTimeout(
        () -> {
          log.info("Emitter for user {} timed out", username);
          emitters.remove(username);
        });

    emitter.onError(
        (e) -> {
          if (isClientDisconnect(e)) {
            log.debug("Emitter for user {} disconnected", username);
          } else {
            log.error("Emitter for user {} had an error", username, e);
          }
          emitters.remove(username);
        });

    try {
      emitter.send(SseEmitter.event().name("connected").data("Connected to notifications"));
    } catch (IOException e) {
      log.error("Error sending initial connection event to {}", username, e);
      emitters.remove(username);
    }

    return emitter;
  }

  public void sendNotification(String username, String message) {
    sendNotification(username, NotificationPayload.info("notification", message, null));
  }

  public void sendNotification(String username, NotificationPayload payload) {
    CompletableFuture.runAsync(() -> sendNotificationNow(username, payload));
  }

  private void sendNotificationNow(String username, NotificationPayload payload) {
    SseEmitter emitter = emitters.get(username);
    if (emitter != null) {
      try {
        emitter.send(SseEmitter.event().name("notification").data(payload));
      } catch (IOException e) {
        log.error("Error sending notification to user {}", username, e);
        emitters.remove(username);
      }
    } else {
      log.debug("User {} is not connected, skipping notification", username);
    }
  }

  public void broadcastNotification(String message) {
    broadcastNotification(NotificationPayload.info("notification", message, null));
  }

  public void broadcastNotification(NotificationPayload payload) {
    for (Map.Entry<String, SseEmitter> entry : emitters.entrySet()) {
      sendNotification(entry.getKey(), payload);
    }
  }

  public void createAndSendNotification(List<String> userIds, String message) {
    createAndSendNotification(userIds, NotificationPayload.info("notification", message, null));
  }

  public void createAndSendNotification(List<String> userIds, NotificationPayload payload) {
    if (userIds == null || userIds.isEmpty()) {
      broadcastNotification(payload);
    } else {
      for (String username : userIds) {
        sendNotification(username, payload);
      }
    }
  }

  public record NotificationPayload(
      String type,
      String message,
      String actionLabel,
      String actionUrl,
      String redirectUrl,
      boolean autoRedirect) {
    public static NotificationPayload info(String type, String message, String redirectUrl) {
      return new NotificationPayload(type, message, null, null, redirectUrl, false);
    }

    public static NotificationPayload action(
        String type, String message, String actionLabel, String actionUrl, String redirectUrl) {
      return new NotificationPayload(type, message, actionLabel, actionUrl, redirectUrl, false);
    }

    public static NotificationPayload redirect(String type, String message, String redirectUrl) {
      return new NotificationPayload(type, message, null, null, redirectUrl, true);
    }
  }

  private boolean isClientDisconnect(Throwable throwable) {
    if (throwable == null) {
      return false;
    }

    String className = throwable.getClass().getSimpleName();
    String message =
        throwable.getMessage() == null ? "" : throwable.getMessage().toLowerCase(Locale.ROOT);
    if ("AsyncRequestNotUsableException".equals(className)) {
      return true;
    }

    return message.contains("disconnected client")
        || message.contains("broken pipe")
        || message.contains("connection reset by peer");
  }
}
