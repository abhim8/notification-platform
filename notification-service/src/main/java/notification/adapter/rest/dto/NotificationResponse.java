package notification.adapter.rest.dto;

import notification.domain.channel.DispatchResult;
import notification.domain.model.NotificationStatus;

import java.util.Map;

/**
 * Response DTO for notification send operation
 */
public record NotificationResponse(
    String eventId,
    boolean success,
    NotificationStatus status,
    String message,
    Map<String, DispatchResult> channelResults
) {}
