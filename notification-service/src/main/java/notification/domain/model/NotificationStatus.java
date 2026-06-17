package notification.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum NotificationStatus {
    SUCCESS("success"),
    FAILED("failed"),
    DROPPED("dropped");

    private final String value;

    NotificationStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static NotificationStatus fromString(String value) {
        if (value == null) {
            throw new IllegalArgumentException("NotificationStatus value cannot be null");
        }
        for (NotificationStatus s : values()) {
            if (s.value.equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown NotificationStatus: " + value);
    }

    @Override
    public String toString() {
        return value;
    }
}
