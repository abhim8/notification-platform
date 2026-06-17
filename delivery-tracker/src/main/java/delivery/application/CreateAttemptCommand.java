package delivery.application;

import delivery.domain.channel.Channel;
import delivery.domain.event.EventType;
import delivery.domain.model.DeliveryStatus;

public record CreateAttemptCommand(
    String eventId,
    String userId,
    EventType eventType,
    Channel channel,
    DeliveryStatus status,
    int attemptNumber,
    String messageId,
    String errorMessage
) {}
