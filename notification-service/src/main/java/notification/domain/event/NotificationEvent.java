package notification.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import notification.domain.channel.Channel;
import java.util.List;
import java.util.Map;

/**
 * Represents a notification event received from Kafka or via REST API.
 *
 * The eventId serves as the idempotency key. Events with the same eventId
 * arriving within 24 hours are deduplicated (dropped silently).
 */
public record NotificationEvent(
    @JsonProperty(required = true)
    String eventId,

    @JsonProperty(required = true)
    EventType eventType,

    @JsonProperty(required = true)
    String userId,

    @JsonProperty(required = true)
    List<Channel> channels,

    @JsonProperty(required = true)
    String templateId,

    @JsonProperty(required = true)
    Map<String, Object> payload,

    long createdAt
) {

    /**
     * Factory method to create a NotificationEvent with current timestamp
     */
    public static NotificationEvent create(
            String eventId,
            EventType eventType,
            String userId,
            List<Channel> channels,
            String templateId,
            Map<String, Object> payload) {
        return new NotificationEvent(
                eventId,
                eventType,
                userId,
                channels,
                templateId,
                payload,
                System.currentTimeMillis()
        );
    }

    /**
     * Validates that all required fields are present and valid
     */
    public void validate() {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("eventId cannot be null or empty");
        }
        if (eventType == null) {
            throw new IllegalArgumentException("eventType cannot be null");
        }
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId cannot be null or empty");
        }
        if (channels == null || channels.isEmpty()) {
            throw new IllegalArgumentException("channels cannot be null or empty");
        }
        if (templateId == null || templateId.isBlank()) {
            throw new IllegalArgumentException("templateId cannot be null or empty");
        }
        if (payload == null) {
            throw new IllegalArgumentException("payload cannot be null");
        }
    }
}

