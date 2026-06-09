package notification.application.service;

/**
 * Port for checking and managing event deduplication.
 *
 * Ensures that duplicate events (same eventId) arriving within 24 hours
 * are detected and skipped to prevent sending duplicate notifications.
 *
 * This is a port interface - the actual Redis implementation will be in infrastructure layer.
 */
public interface DeduplicationService {

    /**
     * Check if an event has been seen before within the deduplication window.
     *
     * @param eventId the unique event identifier
     * @return true if the event is a duplicate (already processed), false if new
     */
    boolean isDuplicate(String eventId);

    /**
     * Register an event as processed.
     * Stores the event ID with a 24-hour TTL in Redis.
     *
     * @param eventId the unique event identifier
     */
    void markProcessed(String eventId);

    /**
     * Remove an event from the deduplication cache (useful for testing/admin)
     *
     * @param eventId the unique event identifier
     */
    void remove(String eventId);
}

