package template.domain;

import java.util.Optional;

public interface TemplateRepository {

    Optional<Template> findByIdAndActiveTrue(String templateId);
}
