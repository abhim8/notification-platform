package notification.domain.model;

/**
 * Enumeration of possible delivery statuses for a notification.
 *
 * State transitions:
 *   PENDING -> DELIVERED
 *   PENDING -> FAILED -> RETRYING -> DELIVERED
 *   PENDING -> FAILED -> RETRYING -> (3x) FAILED -> DLQ
 */
public enum DeliveryStatus {
    /**
     * Waiting to be delivered
     */
    PENDING,

    /**
     * Successfully delivered to recipient/channel
     */
    DELIVERED,

    /**
     * Delivery failed, will be retried
     */
    FAILED,

    /**
     * Currently retrying delivery after a failure
     */
    RETRYING,

    /**
     * Permanently failed after all retries exhausted, sent to DLQ
     */
    DLQ,

    /**
     * Delivery cancelled or skipped (e.g., duplicate within 24h)
     */
    DROPPED
}

