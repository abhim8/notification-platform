package notification.adapter.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import notification.adapter.rest.dto.NotificationRequest;
import notification.adapter.rest.dto.NotificationResponse;
import notification.application.usecase.SendNotificationResult;
import notification.application.usecase.SendNotificationUseCase;
import notification.domain.event.EventType;
import notification.domain.event.NotificationEvent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST controller for sending notifications via the notification-service.
 *
 * Exposes endpoints for:
 * 1. Manually sending notifications (bypassing Kafka)
 * 2. Querying delivery status of sent notifications
 * 3. Getting delivery attempts per channel
 */
@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Notification sending and status tracking")
@Slf4j
public class NotificationController {

    private final SendNotificationUseCase sendNotificationUseCase;

    public NotificationController(SendNotificationUseCase sendNotificationUseCase) {
        this.sendNotificationUseCase = sendNotificationUseCase;
    }

    /**
     * Manually send a notification (synchronous)
     *
     * This endpoint bypasses Kafka and sends notifications directly.
     * Useful for testing or synchronous notification needs.
     */
    @PostMapping
    @Operation(
            summary = "Send a notification",
            description = "Manually sends a notification to specified channels"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Notification sent",
                    content = @Content(schema = @Schema(implementation = NotificationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public ResponseEntity<NotificationResponse> sendNotification(
            @RequestBody NotificationRequest request) {

        try {
            log.info("[REST] Sending notification: eventId={}, eventType={}, userId={}, channels={}",
                    request.eventId(), request.eventType(), request.userId(), request.channels());

            // Validate request
            if (request.eventId() == null || request.eventId().isBlank()) {
                return ResponseEntity.badRequest().build();
            }
            if (request.userId() == null || request.userId().isBlank()) {
                return ResponseEntity.badRequest().build();
            }
            if (request.channels() == null || request.channels().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            if (request.templateId() == null || request.templateId().isBlank()) {
                return ResponseEntity.badRequest().build();
            }

            // Parse event type
            EventType eventType;
            try {
                eventType = EventType.valueOf(request.eventType());
            } catch (IllegalArgumentException e) {
                log.warn("[REST] Invalid event type: {}", request.eventType());
                return ResponseEntity.badRequest().build();
            }

            // Create notification event
            NotificationEvent event = NotificationEvent.create(
                    request.eventId(),
                    eventType,
                    request.userId(),
                    request.channels(),
                    request.templateId(),
                    request.payload() != null ? request.payload() : Map.of()
            );

            // Send notification
            SendNotificationResult result = sendNotificationUseCase.execute(event);

            // Build response
            NotificationResponse response = new NotificationResponse(
                    result.eventId(),
                    result.success(),
                    result.status(),
                    result.message(),
                    result.channelResults()
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("[ERROR] Failed to send notification", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get delivery status for a notification
     *
     * Returns the overall delivery status and per-channel results.
     */
    @GetMapping("/{eventId}")
    @Operation(
            summary = "Get notification delivery status",
            description = "Retrieves the delivery status of a sent notification"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Delivery status retrieved",
                    content = @Content(schema = @Schema(implementation = DeliveryStatusResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Notification not found"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public ResponseEntity<DeliveryStatusResponse> getDeliveryStatus(
            @Parameter(description = "Event ID", required = true)
            @PathVariable String eventId) {

        try {
            log.debug("[REST] Getting delivery status: eventId={}", eventId);

            // In a real system, this would query a delivery status repository
            // For now, we'll return a placeholder indicating the event was processed
            DeliveryStatusResponse response = new DeliveryStatusResponse(
                    eventId,
                    "UNKNOWN",  // Would be PENDING, DELIVERED, FAILED, DLQ, DROPPED
                    "Event has been processed. Query delivery-tracker service for detailed status."
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("[ERROR] Failed to get delivery status: eventId={}", eventId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all delivery attempts for a notification per channel
     *
     * Returns detailed attempt history including timestamps and errors.
     */
    @GetMapping("/{eventId}/attempts")
    @Operation(
            summary = "Get delivery attempts",
            description = "Retrieves all delivery attempts per channel for a notification event"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Delivery attempts retrieved"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Event not found"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    public ResponseEntity<AttemptsResponse> getDeliveryAttempts(
            @Parameter(description = "Event ID", required = true)
            @PathVariable String eventId) {

        try {
            log.debug("[REST] Getting delivery attempts: eventId={}", eventId);

            // In a real system, this would call the delivery-tracker service
            // For now, return a placeholder
            AttemptsResponse response = new AttemptsResponse(
                    eventId,
                    "Query delivery-tracker service at http://localhost:8003/api/v1/delivery-attempts/events/" + eventId
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("[ERROR] Failed to get delivery attempts: eventId={}", eventId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Response DTO for delivery status
     */
    public record DeliveryStatusResponse(String eventId, String overallStatus, String message) {}

    /**
     * Response DTO for delivery attempts
     */
    public record AttemptsResponse(String eventId, String message) {}
}
