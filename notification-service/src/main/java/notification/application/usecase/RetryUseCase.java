package notification.application.usecase;

import lombok.extern.slf4j.Slf4j;
import notification.application.service.DeliveryAttemptRecorder;
import notification.application.service.FailedDeliveryLoader;
import notification.domain.channel.Channel;
import notification.domain.event.EventType;
import notification.domain.model.DeliveryStatus;
import notification.domain.model.RetryPolicy;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Use case for retrying failed notification deliveries.
 *
 * Loads failed delivery attempts from the database and re-tries them
 * according to the retry policy (exponential backoff).
 *
 * This is executed by a scheduled task (ShedLock protected).
 */
@Slf4j
public class RetryUseCase {

    private final RetryPolicy retryPolicy;
    private final SendNotificationUseCase sendNotificationUseCase;
    private final FailedDeliveryLoader failedDeliveryLoader;
    private final DeliveryAttemptRecorder attemptRecorder;

    public RetryUseCase(
            RetryPolicy retryPolicy,
            SendNotificationUseCase sendNotificationUseCase,
            FailedDeliveryLoader failedDeliveryLoader,
            DeliveryAttemptRecorder attemptRecorder) {
        this.retryPolicy = retryPolicy;
        this.sendNotificationUseCase = sendNotificationUseCase;
        this.failedDeliveryLoader = failedDeliveryLoader;
        this.attemptRecorder = attemptRecorder;
    }

    /**
     * Execute the retry use case.
     * Loads all failed deliveries and attempts to retry them.
     *
     * @return count of retries attempted
     */
    public RetryResult execute() {
        log.info("[RETRY] Starting retry scheduler");

        // Load all failed deliveries that should be retried
        var failedDeliveries = failedDeliveryLoader.loadFailedDeliveries();

        int retryCount = 0;
        int dlqCount = 0;

        for (var delivery : failedDeliveries) {
            int attemptCount = delivery.attemptCount();

            // Check if we've exhausted retries
            if (!retryPolicy.canRetry(attemptCount)) {
                log.warn("[DLQ] Event exhausted retries: eventId={}, attempts={}, channel={}",
                        delivery.eventId(), attemptCount, delivery.channel());
                dlqCount++;
                // Mark as DLQ (will be handled by infrastructure layer to publish to DLQ topic)
                attemptRecorder.recordAttempt(new DeliveryAttemptCommand(
                        delivery.eventId(),
                        delivery.userId(),
                        delivery.eventType(),
                        delivery.channel(),
                        DeliveryStatus.DLQ,
                        delivery.attemptCount(),
                        null,
                        "Exhausted retries"
                ));
                continue;
            }

            // Calculate delay for this attempt
            long delayMs = retryPolicy.getDelayForAttempt(attemptCount);
            long timeSinceLastAttempt = Duration.between(delivery.lastAttemptTime(), LocalDateTime.now()).toMillis();

            // Check if enough time has passed since last attempt
            if (timeSinceLastAttempt < delayMs) {
                log.debug("[SKIP] Event not ready for retry yet: eventId={}, waitMs={}",
                        delivery.eventId(), delayMs - timeSinceLastAttempt);
                continue;
            }

            // Retry the delivery
            log.info("[RETRY] Retrying delivery: eventId={}, channel={}, attempt={}",
                    delivery.eventId(), delivery.channel(), attemptCount + 1);

            attemptRecorder.recordAttempt(new DeliveryAttemptCommand(
                    delivery.eventId(),
                    delivery.userId(),
                    delivery.eventType(),
                    delivery.channel(),
                    DeliveryStatus.RETRYING,
                    attemptCount + 1,
                    null,
                    null
            ));
            retryCount++;
        }

        log.info("[RETRY] Completed: retried={}, dlq={}", retryCount, dlqCount);
        return new RetryResult(retryCount, dlqCount);
    }

    /**
     * Result of the retry execution
     */
    public record RetryResult(int retriedCount, int dlqCount) {}

    /**
     * Represents a failed delivery that can be retried
     */
    public record FailedDelivery(
        String eventId,
        String userId,
        EventType eventType,
        Channel channel,
        int attemptCount,
        LocalDateTime lastAttemptTime
    ) {}
}

