package com.notification.common.domain;

public enum DeliveryStatus {
    PENDING,
    DELIVERED,
    FAILED,
    RETRYING,
    DLQ,
    DROPPED
}
