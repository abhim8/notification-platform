package notification.application.usecase;

import notification.domain.channel.Channel;
import notification.domain.event.EventType;
import notification.domain.model.DeliveryStatus;

public record DeliveryAttemptCommand(
    String eventId,
    String userId,
    EventType eventType,
    Channel channel,
    DeliveryStatus status,
    int attemptNumber,
    String messageId,
    String errorMessage
) {}
