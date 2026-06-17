package notification.adapter.rest.dto;

import com.notification.common.domain.Channel;
import com.notification.common.domain.EventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public record NotificationRequest(
    @NotBlank String eventId,
    @NotNull EventType eventType,
    @NotBlank String userId,
    @NotEmpty List<Channel> channels,
    @NotBlank String templateId,
    Map<String, Object> payload
) {}
