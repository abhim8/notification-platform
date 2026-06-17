package delivery.adapter.rest.dto;

import delivery.domain.channel.Channel;
import delivery.domain.event.EventType;
import delivery.domain.model.DeliveryStatus;

import java.time.LocalDateTime;

/**
 * DTO for delivery attempt REST API responses
 */
public record DeliveryAttemptResponse(
    Long id,
    String eventId,
    String userId,
    EventType eventType,
    Channel channel,
    DeliveryStatus status,
    Integer attemptNumber,
    String messageId,
    String errorMessage,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
