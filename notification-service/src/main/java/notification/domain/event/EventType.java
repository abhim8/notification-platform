package notification.domain.event;

/**
 * Enumeration of supported notification event types.
 * Each event type can be routed to different channels and use different templates.
 */
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

    public String getDefaultTemplateId() {
        return defaultTemplateId;
    }
}

