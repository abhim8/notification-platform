package template.domain;

import java.util.List;
import java.util.Optional;

public interface TemplateRepository {

    Optional<Template> findByIdAndActiveTrue(String templateId);

    List<Template> findAll();
}
