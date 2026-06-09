package notification.domain.model;

import java.util.List;

/**
 * Policy defining retry behavior for failed deliveries.
 *
 * Implements exponential backoff with fixed intervals:
 * - Retry 1: 1 second after initial failure
 * - Retry 2: 5 seconds after retry 1
 * - Retry 3: 30 seconds after retry 2
 * - After 3 failures: publish to DLQ
 */
public record RetryPolicy(
    int maxRetries,
    List<Long> backoffDelaysMs
) {

    /**
     * Default retry policy: 3 retries with exponential backoff
     */
    public static RetryPolicy defaultPolicy() {
        return new RetryPolicy(
                3,
                List.of(1_000L, 5_000L, 30_000L)
        );
    }

    /**
     * Get the delay (in milliseconds) for the given retry attempt number
     *
     * @param attemptNumber 0-based retry attempt (0 = first retry)
     * @return delay in milliseconds, or 0 if no more retries available
     */
    public long getDelayForAttempt(int attemptNumber) {
        if (attemptNumber < 0 || attemptNumber >= backoffDelaysMs.size()) {
            return 0;
        }
        return backoffDelaysMs.get(attemptNumber);
    }

    /**
     * Check if we can retry after the given failure count
     */
    public boolean canRetry(int failureCount) {
        return failureCount < maxRetries;
    }
}

