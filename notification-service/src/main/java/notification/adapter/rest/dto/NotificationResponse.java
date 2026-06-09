package notification.adapter.rest.dto;

import notification.domain.channel.DispatchResult;

import java.util.Map;

/**
 * Response DTO for notification send operation
 */
public record NotificationResponse(
    String eventId,
    boolean success,
    String status,  // "success", "failed", "dropped"
    String message,
    Map<String, DispatchResult> channelResults
) {}

