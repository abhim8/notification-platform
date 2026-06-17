package notification.infrastructure.deliverytracker;

import java.time.ZonedDateTime;

record FailedDeliveryDto(
    String eventId,
    String userId,
    String eventType,
    String channel,
    int attemptNumber,
    ZonedDateTime updatedAt
) {}
