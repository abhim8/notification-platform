package notification.adapter.rest.dto;

import com.notification.common.domain.Channel;
import com.notification.common.domain.DeliveryStatus;
import notification.domain.channel.DispatchResult;
import notification.domain.model.NotificationStatus;

import java.util.Map;

public record NotificationResponse(
    String eventId,
    boolean success,
    NotificationStatus status,
    String message,
    Map<String, DispatchResult> channelResults
) {}
