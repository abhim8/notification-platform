package template.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import template.domain.Template;
import template.domain.TemplateRepository;

import java.util.Map;

/**
 * Use case for retrieving and rendering notification templates.
 *
 * Loads templates from the database and renders them with the provided payload.
 */
@Service
@Slf4j
public class TemplateUseCase {

    private final TemplateRepository repository;

    public TemplateUseCase(TemplateRepository repository) {
        this.repository = repository;
    }

    /**
     * Get a template by ID and render it with the given payload
     *
     * @param templateId the template ID to retrieve
     * @param payload the data to inject into the template
     * @return the rendered template
     * @throws TemplateNotFoundException if template not found
     */
    public String getAndRenderTemplate(String templateId, Map<String, Object> payload) {
        log.debug("[TEMPLATE] Retrieving and rendering template: templateId={}", templateId);

        Template template = getTemplate(templateId);

        // Render the body with payload
        return template.renderBody(payload);
    }

    /**
     * Get a template by ID without rendering
     *
     * @param templateId the template ID
     * @return the template domain model
     * @throws TemplateNotFoundException if not found
     */
    public Template getTemplate(String templateId) {
        log.debug("[TEMPLATE] Retrieving template: templateId={}", templateId);

        Template template = repository.findByIdAndActiveTrue(templateId)
                .orElseThrow(() -> {
                    log.warn("[NOT_FOUND] Template not found or inactive: templateId={}", templateId);
                    return new TemplateNotFoundException("Template not found: " + templateId);
                });

        log.debug("[SUCCESS] Template retrieved: templateId={}, eventType={}", templateId, template.eventType());
        return template;
    }

    /**
     * Check if a template exists and is active
     *
     * @param templateId the template ID
     * @return true if exists and active, false otherwise
     */
    public boolean templateExists(String templateId) {
        return repository.findByIdAndActiveTrue(templateId).isPresent();
    }
}
