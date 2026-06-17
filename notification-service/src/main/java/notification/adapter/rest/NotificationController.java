package notification.adapter.rest;

import com.notification.common.domain.DeliveryStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import notification.adapter.rest.dto.NotificationRequest;
import notification.adapter.rest.dto.NotificationResponse;
import notification.application.usecase.SendNotificationResult;
import notification.application.usecase.SendNotificationUseCase;
import notification.domain.event.NotificationEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@Tag(name = "Notifications", description = "Notification sending and status tracking")
@Slf4j
public class NotificationController {

    private final SendNotificationUseCase sendNotificationUseCase;

    public NotificationController(SendNotificationUseCase sendNotificationUseCase) {
        this.sendNotificationUseCase = sendNotificationUseCase;
    }

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
            @Valid @RequestBody NotificationRequest request) {

        log.info("[REST] Sending notification: eventId={}, eventType={}, userId={}, channels={}",
                request.eventId(), request.eventType(), request.userId(), request.channels());

        NotificationEvent event = NotificationEvent.create(
                request.eventId(),
                request.eventType(),
                request.userId(),
                request.channels(),
                request.templateId(),
                request.payload() != null ? request.payload() : Map.of()
        );

        SendNotificationResult result = sendNotificationUseCase.execute(event);

        NotificationResponse response = new NotificationResponse(
                result.eventId(),
                result.success(),
                result.status(),
                result.message(),
                result.channelResults()
        );

        return ResponseEntity.ok(response);
    }

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

        log.debug("[REST] Getting delivery status: eventId={}", eventId);

        DeliveryStatusResponse response = new DeliveryStatusResponse(
                eventId,
                null,
                "Event has been processed. Query delivery-tracker service for detailed status."
        );

        return ResponseEntity.ok(response);
    }

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

        log.debug("[REST] Getting delivery attempts: eventId={}", eventId);

        AttemptsResponse response = new AttemptsResponse(
                eventId,
                "Query delivery-tracker service at http://localhost:8003/api/v1/delivery-attempts/events/" + eventId
        );

        return ResponseEntity.ok(response);
    }

    public record DeliveryStatusResponse(String eventId, DeliveryStatus overallStatus, String message) {}

    public record AttemptsResponse(String eventId, String message) {}
}
