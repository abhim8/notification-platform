package delivery.application;

import com.notification.common.domain.Channel;
import com.notification.common.domain.EventType;
import com.notification.common.domain.DeliveryStatus;

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
