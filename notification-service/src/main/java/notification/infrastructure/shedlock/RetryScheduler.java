package notification.infrastructure.shedlock;

import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import notification.application.usecase.RetryUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task for retrying failed notifications.
 *
 * Runs every 30 seconds (configurable via application.yml)
 * Protected by ShedLock to ensure only one instance runs the retry task
 * across a distributed cluster.
 */
@Component
@Slf4j
public class RetryScheduler {

    private final RetryUseCase retryUseCase;

    public RetryScheduler(RetryUseCase retryUseCase) {
        this.retryUseCase = retryUseCase;
    }

    /**
     * Retry scheduler - runs every 60 seconds
     * Protected by ShedLock for distributed deployment
     */
    @Scheduled(fixedDelayString = "${retry.scheduler.interval-ms:30000}", initialDelayString = "${retry.scheduler.initial-delay-ms:10000}")
    @SchedulerLock(
            name = "notification-retry-scheduler",
            lockAtMostFor = "2m",
            lockAtLeastFor = "30s"
    )
    public void retryFailedNotifications() {
        try {
            log.debug("[SCHEDULER] Starting notification retry scheduler");

            RetryUseCase.RetryResult result = retryUseCase.execute();

            if (result.retriedCount() > 0 || result.dlqCount() > 0) {
                log.info("[SCHEDULER] Retry scheduler completed: retried={}, dlq={}",
                        result.retriedCount(), result.dlqCount());
            } else {
                log.debug("[SCHEDULER] No retries needed");
            }

        } catch (Exception e) {
            log.error("[ERROR] Retry scheduler failed", e);
            // Don't throw - let scheduler retry on next cycle
        }
    }
}

