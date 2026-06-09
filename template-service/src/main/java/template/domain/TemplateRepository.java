package template.domain;

import java.util.Optional;

/**
 * Domain port for loading notification templates.
 */
public interface TemplateRepository {

    Optional<Template> findActiveById(String templateId);
}
