package com.notification.common.dto;

public record ValidationError(
    String field,
    String message,
    Object rejectedValue
) {
    public ValidationError(String field, String message) {
        this(field, message, null);
    }
}
