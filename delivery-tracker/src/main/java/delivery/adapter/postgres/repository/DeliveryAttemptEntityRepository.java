package delivery.adapter.postgres.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import delivery.adapter.postgres.entity.DeliveryAttemptEntity;

import java.time.ZonedDateTime;
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
    List<DeliveryAttemptEntity> findByEventIdAndChannel(String eventId, String channel);

    /**
     * Find attempts by event ID and user ID
     */
    List<DeliveryAttemptEntity> findByEventIdAndUserId(String eventId, String userId);

    /**
     * Find attempts for a user created after a given timestamp
     */
    List<DeliveryAttemptEntity> findByUserIdAndCreatedAtAfter(String userId, ZonedDateTime since);

    /**
     * Find attempts with given status
     */
    List<DeliveryAttemptEntity> findByStatus(String status);

    /**
     * Count attempts by event ID and channel
     */
    int countByEventIdAndChannel(String eventId, String channel);
}

