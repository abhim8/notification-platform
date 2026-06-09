package delivery.adapter.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import delivery.adapter.postgres.entity.DeliveryAttemptEntity;
import delivery.adapter.postgres.repository.DeliveryAttemptEntityRepository;
import delivery.adapter.rest.dto.DeliveryAttemptResponse;

import java.util.List;

/**
 * REST controller for delivery attempt tracking.
 */
@RestController
@RequestMapping("/api/v1/delivery-attempts")
@Tag(name = "Delivery Attempts", description = "Delivery attempt tracking and history")
public class DeliveryAttemptController {

    private static final Logger log = LoggerFactory.getLogger(DeliveryAttemptController.class);

    private final DeliveryAttemptEntityRepository repository;

    public DeliveryAttemptController(DeliveryAttemptEntityRepository repository) {
        this.repository = repository;
    }

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

            List<DeliveryAttemptEntity> attempts = repository.findByEventId(eventId);

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

            List<DeliveryAttemptEntity> attempts = repository.findByEventIdAndChannel(eventId, channel);

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

            return repository.findById(attemptId)
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
            log.debug("GET /api/v1/delivery-attempts/users/{}", userId);

            List<DeliveryAttemptEntity> attempts = repository.findByUserIdAndCreatedAtAfter(
                    userId, java.time.ZonedDateTime.now().minusDays(30));

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

