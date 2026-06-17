package com.notification.common.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

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
