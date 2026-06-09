package delivery.domain;

import java.time.ZonedDateTime;

/**
 * Domain model representing a delivery attempt.
 *
 * Tracks a single delivery attempt for a notification event to a specific channel.
 * Multiple attempts may exist per event (one per channel, plus retries).
 */
public record DeliveryAttempt(
    Long id,
    String eventId,
    String userId,
    String eventType,
    String channel,
    String status,
    Integer attemptNumber,
    String messageId,
    String errorMessage,
    ZonedDateTime createdAt,
    ZonedDateTime updatedAt
) {}

