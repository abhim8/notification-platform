package delivery.adapter.postgres.entity;

import com.notification.common.domain.Channel;
import com.notification.common.domain.EventType;
import com.notification.common.domain.DeliveryStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * JPA entity representing a delivery attempt record.
 */
@Setter
@Getter
@Entity
@Table(name = "delivery_attempts",
       indexes = {
           @Index(name = "idx_event_id", columnList = "event_id"),
           @Index(name = "idx_user_id", columnList = "user_id"),
           @Index(name = "idx_channel", columnList = "channel"),
           @Index(name = "idx_status", columnList = "status"),
           @Index(name = "idx_created_at", columnList = "created_at")
       })
public class DeliveryAttemptEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, length = 64)
    private String eventId;

    @Column(name = "user_id", nullable = false, length = 64)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 100)
    private EventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private Channel channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DeliveryStatus status;

    @Column(name = "attempt_number", nullable = false)
    private Integer attemptNumber;

    @Column(name = "message_id", length = 255)
    private String messageId;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public DeliveryAttemptEntity() {}

    public DeliveryAttemptEntity(String eventId, String userId, EventType eventType, Channel channel,
                                DeliveryStatus status, Integer attemptNumber) {
        this.eventId = eventId;
        this.userId = userId;
        this.eventType = eventType;
        this.channel = channel;
        this.status = status;
        this.attemptNumber = attemptNumber;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

}

