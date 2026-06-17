package delivery.adapter.rest;

import delivery.adapter.postgres.entity.DeliveryAttemptEntity;
import delivery.adapter.rest.dto.CreateDeliveryAttemptRequest;
import delivery.adapter.rest.dto.DeliveryAttemptResponse;
import delivery.application.CreateAttemptCommand;
import delivery.application.DeliveryAttemptUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for delivery attempt tracking.
 */
@RestController
@RequestMapping("/api/v1/delivery-attempts")
@Tag(name = "Delivery Attempts", description = "Delivery attempt tracking and history")
@Slf4j
@RequiredArgsConstructor
public class DeliveryAttemptController {

    private final DeliveryAttemptUseCase useCase;

    /**
     * Get all delivery attempts for an event
     */
    @GetMapping("/events/{eventId}")
    @Operation(summary = "Get attempts by event ID", description = "Retrieves all delivery attempts for a specific event")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attempts retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Event not found")
    })
    public ResponseEntity<List<DeliveryAttemptResponse>> getAttemptsByEvent(
            @Parameter(description = "Event ID", required = true)
            @PathVariable String eventId) {

        try {
            log.debug("GET /api/v1/delivery-attempts/events/{}", eventId);

            List<DeliveryAttemptEntity> attempts = useCase.getAttemptsByEvent(eventId);

            if (attempts.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            List<DeliveryAttemptResponse> responses = attempts.stream()
                    .map(this::toResponse)
                    .toList();

            return ResponseEntity.ok(responses);

        } catch (Exception e) {
            log.error("Error retrieving attempts for event: {}", eventId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get delivery attempts for a specific event and channel
     */
    @GetMapping("/events/{eventId}/channels/{channel}")
    @Operation(summary = "Get attempts by event and channel", description = "Retrieves all delivery attempts for a specific event and channel")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attempts retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No attempts found")
    })
    public ResponseEntity<List<DeliveryAttemptResponse>> getAttemptsByEventAndChannel(
            @Parameter(description = "Event ID", required = true)
            @PathVariable String eventId,
            @Parameter(description = "Channel (email, sms, push, webhook)", required = true)
            @PathVariable String channel) {

        try {
            log.debug("GET /api/v1/delivery-attempts/events/{}/channels/{}", eventId, channel);

            List<DeliveryAttemptEntity> attempts = useCase.getAttemptsByEventAndChannel(eventId, channel);

            if (attempts.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            List<DeliveryAttemptResponse> responses = attempts.stream()
                    .map(this::toResponse)
                    .toList();

            return ResponseEntity.ok(responses);

        } catch (Exception e) {
            log.error("Error retrieving attempts for event: {} and channel: {}", eventId, channel, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get a single delivery attempt by ID
     */
    @GetMapping("/{attemptId}")
    @Operation(summary = "Get attempt by ID", description = "Retrieves a single delivery attempt by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attempt found"),
            @ApiResponse(responseCode = "404", description = "Attempt not found")
    })
    public ResponseEntity<DeliveryAttemptResponse> getAttemptById(
            @Parameter(description = "Attempt ID", required = true)
            @PathVariable Long attemptId) {

        try {
            log.debug("GET /api/v1/delivery-attempts/{}", attemptId);

            return useCase.getAttemptById(attemptId)
                    .map(this::toResponse)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            log.error("Error retrieving attempt: {}", attemptId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all attempts for a user ID
     */
    @GetMapping("/users/{userId}")
    @Operation(summary = "Get attempts by user ID", description = "Retrieves all delivery attempts for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attempts retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "No attempts found for user")
    })
    public ResponseEntity<List<DeliveryAttemptResponse>> getAttemptsByUser(
            @Parameter(description = "User ID", required = true)
            @PathVariable String userId) {

        try {
            log.debug("GET /api/v1/delivery-attempts/users/{}/", userId);

            List<DeliveryAttemptEntity> attempts = useCase.getAttemptsByUser(userId);

            if (attempts.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            List<DeliveryAttemptResponse> responses = attempts.stream()
                    .map(this::toResponse)
                    .toList();

            return ResponseEntity.ok(responses);

        } catch (Exception e) {
            log.error("Error retrieving attempts for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Record a delivery attempt
     */
    @PostMapping
    @Operation(summary = "Record delivery attempt", description = "Persists a delivery attempt for tracking and retry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Attempt recorded"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<DeliveryAttemptResponse> createAttempt(
            @Parameter(description = "Delivery attempt data", required = true)
            @RequestBody CreateDeliveryAttemptRequest request) {

        try {
            log.debug("POST /api/v1/delivery-attempts");

            CreateAttemptCommand command = new CreateAttemptCommand(
                    request.eventId(), request.userId(), request.eventType(),
                    request.channel(), request.status(), request.attemptNumber(),
                    request.messageId(), request.errorMessage()
            );

            DeliveryAttemptEntity saved = useCase.createAttempt(command);

            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));

        } catch (Exception e) {
            log.error("Error saving delivery attempt for event: {}", request.eventId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get failed delivery attempts for retry processing
     */
    @GetMapping("/failed")
    @Operation(summary = "Get failed attempts", description = "Retrieves failed delivery attempts for retry processing")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Failed attempts retrieved")
    })
    public ResponseEntity<List<DeliveryAttemptResponse>> getFailedAttempts(
            @Parameter(description = "Only attempts updated after this timestamp (ISO-8601)")
            @RequestParam(required = false) String since,
            @Parameter(description = "Maximum number of attempts to return")
            @RequestParam(defaultValue = "100") int limit) {

        try {
            log.debug("GET /api/v1/delivery-attempts/failed");

            List<DeliveryAttemptEntity> attempts = useCase.getFailedAttempts(since, limit);

            List<DeliveryAttemptResponse> responses = attempts.stream()
                    .map(this::toResponse)
                    .toList();

            return ResponseEntity.ok(responses);

        } catch (Exception e) {
            log.error("Error retrieving failed attempts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Convert entity to DTO
     */
    private DeliveryAttemptResponse toResponse(DeliveryAttemptEntity entity) {
        return new DeliveryAttemptResponse(
                entity.getId(),
                entity.getEventId(),
                entity.getUserId(),
                entity.getEventType(),
                entity.getChannel(),
                entity.getStatus(),
                entity.getAttemptNumber(),
                entity.getMessageId(),
                entity.getErrorMessage(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}

