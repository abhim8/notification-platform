package delivery.adapter.rest.dto;

import com.notification.common.domain.Channel;
import com.notification.common.domain.DeliveryStatus;
import com.notification.common.domain.EventType;

import java.time.LocalDateTime;

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
