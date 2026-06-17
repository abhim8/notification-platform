package template.adapter.postgres.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

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

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "subject", length = 500)
    private String subject;

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "active", nullable = false)
    private Boolean active;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    public TemplateEntity() {}

    public TemplateEntity(String id, String eventType, String name, String subject, String body,
                        Integer version, Boolean active) {
        this.id = id;
        this.eventType = eventType;
        this.name = name;
        this.subject = subject;
        this.body = body;
        this.version = version;
        this.active = active;
        this.createdAt = ZonedDateTime.now();
        this.updatedAt = ZonedDateTime.now();
    }

}

