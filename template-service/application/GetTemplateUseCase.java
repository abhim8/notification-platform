package template.application;

import template.domain.Template;
import template.domain.TemplateRepository;

import java.util.Optional;

public class GetTemplateUseCase {
    private final TemplateRepository templateRepository;

    public GetTemplateUseCase(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    public Template execute(String templateId) throws TemplateNotFoundException {
        Optional<Template> template = templateRepository.findById(templateId);

        if (template.isEmpty()) {
            throw new TemplateNotFoundException("Template not found: " + templateId);
        }

        return template.get();
    }
}

