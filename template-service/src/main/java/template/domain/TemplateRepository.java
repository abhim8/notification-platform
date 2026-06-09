package template.domain;

import java.util.List;
import java.util.Optional;

public interface TemplateRepository {

    void save(Template template);

    Optional<Template> findById(String templateId);

    List<Template> findByEventType(String eventType);

    List<Template> findActiveByEventType(String eventType);

    void update(Template template);

    void delete(String templateId);
}
