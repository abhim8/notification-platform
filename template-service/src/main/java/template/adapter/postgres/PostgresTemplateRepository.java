package template.adapter.postgres;

import org.springframework.stereotype.Repository;
import template.adapter.postgres.entity.TemplateEntity;
import template.adapter.postgres.repository.TemplateEntityRepository;
import template.domain.Template;
import template.domain.TemplateRepository;

import java.util.Optional;

/**
 * PostgreSQL adapter for template persistence.
 */
@Repository
public class PostgresTemplateRepository implements TemplateRepository {

    private final TemplateEntityRepository repository;

    public PostgresTemplateRepository(TemplateEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Template> findActiveById(String templateId) {
        return repository.findByIdAndActiveTrue(templateId)
                .map(this::toDomain);
    }

    private Template toDomain(TemplateEntity entity) {
        return new Template(
                entity.getId(),
                entity.getEventType(),
                entity.getName(),
                entity.getSubject(),
                entity.getBody(),
                entity.getVersion(),
                entity.getActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
