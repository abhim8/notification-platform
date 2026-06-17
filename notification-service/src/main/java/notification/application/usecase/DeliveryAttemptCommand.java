package notification.application.usecase;

import com.notification.common.domain.Channel;
import com.notification.common.domain.EventType;
import com.notification.common.domain.DeliveryStatus;

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
