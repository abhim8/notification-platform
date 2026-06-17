package notification.application.usecase;

import notification.domain.channel.Channel;
import notification.domain.model.DeliveryStatus;

public record DeliveryAttemptCommand(
    String eventId,
    String userId,
    String eventType,
    Channel channel,
    DeliveryStatus status,
    int attemptNumber,
    String messageId,
    String errorMessage
) {}
