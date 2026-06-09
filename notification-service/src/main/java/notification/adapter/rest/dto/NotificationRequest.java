package notification.adapter.rest.dto;

import java.util.Map;

/**
 * Request DTO for creating and sending a notification
 */
public record NotificationRequest(
    String eventId,
    String eventType,
    String userId,
    java.util.List<String> channels,
    String templateId,
    Map<String, Object> payload
) {}

