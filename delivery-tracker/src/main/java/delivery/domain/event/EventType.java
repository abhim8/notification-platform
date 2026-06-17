package delivery.domain.event;

public enum EventType {
    ORDER_PLACED,
    ORDER_SHIPPED,
    ORDER_DELIVERED,
    PAYMENT_FAILED,
    PASSWORD_RESET,
    ACCOUNT_CREATED,
    USER_INVITED,
    PROMOTION_ALERT,
    FRAUD_ALERT,
    SYSTEM_ALERT
}
