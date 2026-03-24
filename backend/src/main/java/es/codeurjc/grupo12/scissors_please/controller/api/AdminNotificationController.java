package es.codeurjc.grupo12.scissors_please.controller.api;

import es.codeurjc.grupo12.scissors_please.service.notification.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@RestController
@RequestMapping("/api/admin/notifications")
@Tag(
    name = "Admin Notifications",
    description = "Operations for sending admin notifications to users")
@RequiredArgsConstructor
public class AdminNotificationController {

  private final NotificationService notificationService;

  @PostMapping
  @Operation(
      summary = "Send a notification to users",
      description = "Creates and sends a notification message to the selected usernames.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Notification sent successfully"),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request payload",
            content = @Content)
      })
  public ResponseEntity<Void> sendNotification(@RequestBody NotificationRequest request) {
    notificationService.createAndSendNotification(request.getUsernames(), request.getMessage());
    return ResponseEntity.ok().build();
  }

  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  @Schema(
      name = "AdminNotificationRequest",
      description = "Payload used to send an admin notification")
  public static class NotificationRequest {
    @Schema(
        description = "Usernames that will receive the notification",
        example = "[\"alice\", \"bob\"]")
    private List<String> usernames;

    @Schema(description = "Notification message to send", example = "Tournament starts in 10 minutes")
    private String message;
  }
}
