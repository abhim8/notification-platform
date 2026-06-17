package delivery.domain.model;

public enum DeliveryStatus {
    PENDING,
    DELIVERED,
    FAILED,
    RETRYING,
    DLQ,
    DROPPED
}
