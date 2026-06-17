package delivery.adapter.postgres.repository;

import delivery.adapter.postgres.entity.DeliveryAttemptEntity;
import com.notification.common.domain.Channel;
import com.notification.common.domain.DeliveryStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * JPA repository for DeliveryAttemptEntity.
 */
@Repository
public interface DeliveryAttemptEntityRepository extends JpaRepository<DeliveryAttemptEntity, Long> {

    /**
     * Find all attempts for a specific event
     */
    List<DeliveryAttemptEntity> findByEventId(String eventId);

    /**
     * Find all attempts for a specific event and channel
     */
    List<DeliveryAttemptEntity> findByEventIdAndChannel(String eventId, Channel channel);

    /**
     * Find attempts by event ID and user ID
     */
    List<DeliveryAttemptEntity> findByEventIdAndUserId(String eventId, String userId);

    /**
     * Find attempts for a user created after a given timestamp
     */
    List<DeliveryAttemptEntity> findByUserIdAndCreatedAtAfter(String userId, LocalDateTime since);

    /**
     * Find attempts with given status
     */
    List<DeliveryAttemptEntity> findByStatus(DeliveryStatus status);

    /**
     * Find failed attempts for retry, ordered by recency
     */
    List<DeliveryAttemptEntity> findByStatusAndUpdatedAtAfterOrderByUpdatedAtAsc(
            DeliveryStatus status, LocalDateTime since, Pageable pageable);

    /**
     * Count attempts by event ID and channel
     */
    int countByEventIdAndChannel(String eventId, Channel channel);
}
