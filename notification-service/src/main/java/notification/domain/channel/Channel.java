package notification.domain.channel;

/**
 * Enumeration of supported notification channels.
 * Each channel has its own adapter implementation.
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

    public String getValue() {
        return value;
    }

    /**
     * Convert string representation to Channel enum
     */
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

