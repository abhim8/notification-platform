package template.adapter.postgres.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.notification.common.domain.EventType;

import java.time.LocalDateTime;

/**
 * JPA entity for Template persistence in PostgreSQL.
 */
@Setter
@Getter
@Entity
@Table(name = "templates",
       indexes = {
           @Index(name = "idx_template_event_type", columnList = "event_type"),
           @Index(name = "idx_template_active", columnList = "active")
       })
public class TemplateEntity {

    @Id
    @Column(name = "id", length = 64)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 100)
    private EventType eventType;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "subject", length = 500)
    private String subject;

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public TemplateEntity() {}

    public TemplateEntity(String id, EventType eventType, String name, String subject, String body,
                        Integer version, Boolean active) {
        this.id = id;
        this.eventType = eventType;
        this.name = name;
        this.subject = subject;
        this.body = body;
        this.version = version;
        this.active = active;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

}

