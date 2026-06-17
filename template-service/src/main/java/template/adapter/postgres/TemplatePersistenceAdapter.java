package template.adapter.postgres;

import org.springframework.stereotype.Repository;
import template.adapter.postgres.repository.TemplateEntityRepository;
import template.domain.Template;
import template.domain.TemplateRepository;

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
                .map(entity -> new Template(
                        entity.getId(), entity.getEventType(), entity.getName(),
                        entity.getSubject(), entity.getBody(), entity.getVersion(),
                        entity.getActive(), entity.getCreatedAt(), entity.getUpdatedAt()
                ));
    }
}
