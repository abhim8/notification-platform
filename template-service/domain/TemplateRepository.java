package template.domain;

import java.util.List;
import java.util.Optional;

public interface TemplateRepository {

    void save(Template template);

    Optional<Template> findById(String templateId);

    List<Template> findByChannel(String channel);

    void update(Template template);

    void delete(String templateId);
}

