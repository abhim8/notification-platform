package template.adapter.postgres.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import template.adapter.postgres.entity.TemplateEntity;
import template.domain.event.EventType;

import java.util.List;
import java.util.Optional;

/**
 * JPA repository for TemplateEntity.
 */
@Repository
public interface TemplateEntityRepository extends JpaRepository<TemplateEntity, String> {

    /**
     * Find all active templates for an event type
     */
    List<TemplateEntity> findByEventTypeAndActiveTrue(EventType eventType);

    /**
     * Find all active templates
     */
    List<TemplateEntity> findByActiveTrue();

    /**
     * Find all templates for an event type (active and inactive)
     */
    List<TemplateEntity> findByEventType(EventType eventType);

    /**
     * Find by ID and ensure it's active
     */
    Optional<TemplateEntity> findByIdAndActiveTrue(String id);
}

