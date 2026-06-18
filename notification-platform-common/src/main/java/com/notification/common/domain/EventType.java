package com.notification.common.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EventType {
    ORDER_PLACED("order-confirm"),
    ORDER_SHIPPED("order-shipped"),
    ORDER_DELIVERED("order-delivered"),
    PAYMENT_FAILED("payment-failed"),
    PASSWORD_RESET("password-reset"),
    ACCOUNT_CREATED("account-created"),
    USER_INVITED("user-invited"),
    PROMOTION_ALERT("promotion-alert"),
    FRAUD_ALERT("fraud-alert"),
    SYSTEM_ALERT("system-alert");

    private final String defaultTemplateId;

    EventType(String defaultTemplateId) {
        this.defaultTemplateId = defaultTemplateId;
    }

    @JsonValue
    public String getDefaultTemplateId() {
        return defaultTemplateId;
    }

    @JsonCreator
    public static EventType fromString(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        for (EventType eventType : EventType.values()) {
            if (eventType.name().equalsIgnoreCase(value)) {
                return eventType;
            }
            if (eventType.defaultTemplateId.equalsIgnoreCase(value)) {
                return eventType;
            }
        }
        throw new IllegalArgumentException("Unknown EventType: " + value);
    }
}
