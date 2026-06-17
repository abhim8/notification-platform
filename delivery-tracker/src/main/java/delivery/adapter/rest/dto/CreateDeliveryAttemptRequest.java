package delivery.adapter.rest.dto;

import com.notification.common.domain.Channel;
import com.notification.common.domain.DeliveryStatus;
import com.notification.common.domain.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateDeliveryAttemptRequest(
    @NotBlank String eventId,
    @NotBlank String userId,
    @NotNull EventType eventType,
    @NotNull Channel channel,
    @NotNull DeliveryStatus status,
    @Positive int attemptNumber,
    String messageId,
    String errorMessage
) {}
