package notification.infrastructure.postgres.adapter;

import lombok.extern.slf4j.Slf4j;
import notification.application.usecase.SendNotificationUseCase;
import notification.domain.channel.Channel;
import notification.domain.model.DeliveryStatus;
import notification.infrastructure.postgres.entity.DeliveryAttemptEntity;
import notification.infrastructure.postgres.repository.DeliveryAttemptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

/**
 * JPA-backed implementation of DeliveryAttemptRecorder.
 * Records all delivery attempts in PostgreSQL for audit and retry purposes.
 */
@Service
@Transactional
@Slf4j
public class PostgresDeliveryAttemptRecorder implements SendNotificationUseCase.DeliveryAttemptRecorder {

    private final DeliveryAttemptRepository repository;

    public PostgresDeliveryAttemptRecorder(DeliveryAttemptRepository repository) {
        this.repository = repository;
    }

    @Override
    public void recordAttempt(String eventId, Channel channel, DeliveryStatus status,
                            String messageId, String errorMessage) {
        try {
            // For now, we'll just log the attempt
            // In a full implementation, we would:
            // 1. Query the event from the database or event store
            // 2. Create a new DeliveryAttemptEntity
            // 3. Save it to the repository

            log.debug("[DB] Recording delivery attempt: eventId={}, channel={}, status={}, messageId={}",
                    eventId, channel, status, messageId);

            // TODO: Implement full persistence logic once event storage is set up
            // For now, just acknowledge the attempt was recorded

        } catch (Exception e) {
            log.error("[ERROR] Failed to record delivery attempt: eventId={}, channel={}",
                    eventId, channel, e);
            // Don't throw - allow processing to continue even if recording fails
        }
    }

    /**
     * Helper method to create and save an attempt entity (used internally)
     */
    public void saveAttempt(String eventId, String userId, String eventType, String channel,
                           String status, Integer attemptNumber, String messageId, String errorMessage) {
        try {
            DeliveryAttemptEntity attempt = new DeliveryAttemptEntity(
                    eventId,
                    userId,
                    eventType,
                    channel,
                    status,
                    attemptNumber
            );

            attempt.setMessageId(messageId);
            attempt.setErrorMessage(errorMessage);
            attempt.setUpdatedAt(ZonedDateTime.now());

            repository.save(attempt);

            log.debug("[DB] Delivery attempt saved: eventId={}, channel={}, id={}",
                    eventId, channel, attempt.getId());

        } catch (Exception e) {
            log.error("[ERROR] Failed to save delivery attempt: eventId={}, channel={}",
                    eventId, channel, e);
        }
    }
}
