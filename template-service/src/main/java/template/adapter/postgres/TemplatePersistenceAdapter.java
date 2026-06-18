package template.adapter.postgres;

import org.springframework.stereotype.Repository;
import template.adapter.postgres.entity.TemplateEntity;
import template.adapter.postgres.repository.TemplateEntityRepository;
import template.domain.Template;
import template.domain.TemplateRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class TemplatePersistenceAdapter implements TemplateRepository {

    private final TemplateEntityRepository repository;

    public TemplatePersistenceAdapter(TemplateEntityRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<Template> findByIdAndActiveTrue(String templateId) {
        return repository.findByIdAndActiveTrue(templateId)
                .map(this::toDomain);
    }

    @Override
    public List<Template> findAll() {
        return repository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    private Template toDomain(TemplateEntity entity) {
        return new Template(
                entity.getId(), entity.getEventType(), entity.getName(),
                entity.getSubject(), entity.getBody(), entity.getVersion(),
                entity.isActive(), entity.getCreatedAt(), entity.getUpdatedAt()
        );
    }
}
