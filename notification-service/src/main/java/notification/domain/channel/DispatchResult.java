package notification.domain.channel;

/**
 * Result of a channel dispatch attempt.
 * Contains success/failure status and optional error details.
 */
public record DispatchResult(
    boolean success,
    String messageId,
    String errorMessage
) {

    /**
     * Create a successful dispatch result
     */
    public static DispatchResult success(String messageId) {
        return new DispatchResult(true, messageId, null);
    }

    /**
     * Create a failed dispatch result
     */
    public static DispatchResult failure(String errorMessage) {
        return new DispatchResult(false, null, errorMessage);
    }
}

