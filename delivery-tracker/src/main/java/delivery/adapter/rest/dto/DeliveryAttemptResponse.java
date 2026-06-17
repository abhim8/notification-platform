package delivery.adapter.rest.dto;

import java.time.LocalDateTime;

/**
 * DTO for delivery attempt REST API responses
 */
public record DeliveryAttemptResponse(
    Long id,
    String eventId,
    String userId,
    String eventType,
    String channel,
    String status,
    Integer attemptNumber,
    String messageId,
    String errorMessage,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

