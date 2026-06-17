package notification.adapter.rest.dto;

import notification.domain.channel.Channel;
import notification.domain.event.EventType;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for creating and sending a notification
 */
public record NotificationRequest(
    String eventId,
    EventType eventType,
    String userId,
    List<Channel> channels,
    String templateId,
    Map<String, Object> payload
) {}

