package notification.infrastructure.deliverytracker;

import java.time.LocalDateTime;

record FailedDeliveryDto(
    String eventId,
    String userId,
    String eventType,
    String channel,
    int attemptNumber,
    LocalDateTime updatedAt
) {}
