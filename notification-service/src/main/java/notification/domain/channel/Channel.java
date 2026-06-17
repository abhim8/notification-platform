package notification.domain.channel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enumeration of supported notification channels.
 * Each channel has its own adapter implementation.
 *
 * <p>Uses {@link JsonValue} for serialization (lowercase) and
 * {@link JsonCreator} for case-insensitive deserialization.</p>
 */
public enum Channel {
    EMAIL("email"),
    SMS("sms"),
    PUSH("push"),
    WEBHOOK("webhook");

    private final String value;

    Channel(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * Returns the payload key used to look up the recipient for this channel.
     * Format: {@code <channel_value>_recipient} (e.g., {@code email_recipient})
     */
    public String recipientKey() {
        return value + "_recipient";
    }

    @JsonCreator
    public static Channel fromString(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Channel value cannot be null or empty");
        }
        for (Channel channel : Channel.values()) {
            if (channel.value.equalsIgnoreCase(value)) {
                return channel;
            }
        }
        throw new IllegalArgumentException("Unknown channel: " + value);
    }
}

