package delivery.application;

import delivery.adapter.postgres.entity.DeliveryAttemptEntity;
import delivery.adapter.postgres.repository.DeliveryAttemptEntityRepository;
import com.notification.common.domain.Channel;
import com.notification.common.domain.DeliveryStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.notification.common.exception.BadRequestException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class DeliveryAttemptUseCase {

    private final DeliveryAttemptEntityRepository repository;

    public DeliveryAttemptUseCase(DeliveryAttemptEntityRepository repository) {
        this.repository = repository;
    }

    public List<DeliveryAttemptEntity> getAttemptsByEvent(String eventId) {
        log.debug("Fetching attempts for event: {}", eventId);
        return repository.findByEventId(eventId);
    }

    public List<DeliveryAttemptEntity> getAttemptsByEventAndChannel(String eventId, Channel channel) {
        log.debug("Fetching attempts for event: {} and channel: {}", eventId, channel);
        return repository.findByEventIdAndChannel(eventId, channel);
    }

    public Optional<DeliveryAttemptEntity> getAttemptById(Long attemptId) {
        return repository.findById(attemptId);
    }

    public List<DeliveryAttemptEntity> getAttemptsByUser(String userId) {
        log.debug("Fetching attempts for user: {}", userId);
        return repository.findByUserIdAndCreatedAtAfter(userId, LocalDateTime.now().minusDays(30));
    }

    public DeliveryAttemptEntity createAttempt(CreateAttemptCommand command) {
        log.debug("Creating delivery attempt: eventId={}, channel={}", command.eventId(), command.channel());

        DeliveryAttemptEntity entity = new DeliveryAttemptEntity(
                command.eventId(), command.userId(), command.eventType(),
                command.channel(), command.status(), command.attemptNumber()
        );
        entity.setMessageId(command.messageId());
        entity.setErrorMessage(command.errorMessage());

        DeliveryAttemptEntity saved = repository.save(entity);
        log.debug("Delivery attempt saved: id={}, eventId={}", saved.getId(), saved.getEventId());
        return saved;
    }

    public List<DeliveryAttemptEntity> getFailedAttempts(String since, int limit) {
        LocalDateTime cutoff = since != null
                ? parseDateTime(since)
                : LocalDateTime.now().minusDays(1);
        return repository.findByStatusAndUpdatedAtAfterOrderByUpdatedAtAsc(
                DeliveryStatus.FAILED, cutoff, PageRequest.of(0, limit));
    }

    private static LocalDateTime parseDateTime(String value) {
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException e) {
            try {
                return OffsetDateTime.parse(value).toLocalDateTime();
            } catch (DateTimeParseException ex) {
                throw new BadRequestException(
                        "Invalid date format: '" + value
                        + "'. Expected ISO-8601 format (e.g., 2026-06-16T00:00:00 or 2026-06-16T00:00:00Z)");
            }
        }
    }
}
