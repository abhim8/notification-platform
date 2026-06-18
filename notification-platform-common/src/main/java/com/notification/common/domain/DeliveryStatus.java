package com.notification.common.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum DeliveryStatus {
    PENDING,
    DELIVERED,
    FAILED,
    RETRYING,
    DLQ,
    DROPPED;

    @JsonValue
    public String toValue() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static DeliveryStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (DeliveryStatus status : DeliveryStatus.values()) {
            if (status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown DeliveryStatus: " + value);
    }
}
