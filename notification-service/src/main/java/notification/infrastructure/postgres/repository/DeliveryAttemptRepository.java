package notification.infrastructure.postgres.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import notification.infrastructure.postgres.entity.DeliveryAttemptEntity;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * JPA repository for DeliveryAttemptEntity.
 * Handles all database operations for delivery attempt tracking.
 */
@Repository
public interface DeliveryAttemptRepository extends JpaRepository<DeliveryAttemptEntity, Long> {

    /**
     * Find all attempts for a specific event
     */
    List<DeliveryAttemptEntity> findByEventId(String eventId);

    /**
     * Find all attempts for a specific event and channel
     */
    Optional<DeliveryAttemptEntity> findByEventIdAndChannel(String eventId, String channel);

    /**
     * Find all failed attempts that should be retried
     * Status is FAILED and created_at is older than the retry window
     */
    @Query("""
        SELECT da FROM DeliveryAttemptEntity da 
        WHERE da.status = 'FAILED' 
        AND da.updatedAt > :since
        ORDER BY da.updatedAt ASC
        """)
    List<DeliveryAttemptEntity> findFailedAttemptsForRetry(@Param("since") ZonedDateTime since);

    /**
     * Find attempts by event ID and user ID
     */
    List<DeliveryAttemptEntity> findByEventIdAndUserId(String eventId, String userId);

    /**
     * Count attempts by event ID and channel
     */
    int countByEventIdAndChannel(String eventId, String channel);

    /**
     * Find attempts with given status
     */
    List<DeliveryAttemptEntity> findByStatus(String status);

    /**
     * Find attempts for a user created after a given timestamp
     */
    List<DeliveryAttemptEntity> findByUserIdAndCreatedAtAfter(String userId, ZonedDateTime since);
}

