package es.codeurjc.grupo12.scissors_please.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
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
          log.error("Emitter for user {} had an error", username, e);
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
    SseEmitter emitter = emitters.get(username);
    if (emitter != null) {
      try {
        emitter.send(SseEmitter.event().name("notification").data(message));
      } catch (IOException e) {
        log.error("Error sending notification to user {}", username, e);
        emitters.remove(username);
      }
    } else {
      log.debug("User {} is not connected, skipping notification", username);
    }
  }

  public void broadcastNotification(String message) {
    for (Map.Entry<String, SseEmitter> entry : emitters.entrySet()) {
      String username = entry.getKey();
      SseEmitter emitter = entry.getValue();
      try {
        emitter.send(SseEmitter.event().name("notification").data(message));
      } catch (IOException e) {
        log.error("Error broadcasting notification to user {}", username, e);
        emitters.remove(username);
      }
    }
  }

  public void createAndSendNotification(List<String> userIds, String message) {
    if (userIds == null || userIds.isEmpty()) {
      broadcastNotification(message);
    } else {
      for (String username : userIds) {
        sendNotification(username, message);
      }
    }
  }
}
