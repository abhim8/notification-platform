package template.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import template.adapter.postgres.entity.TemplateEntity;
import template.adapter.postgres.repository.TemplateEntityRepository;
import template.domain.Template;

import java.util.Map;
import java.util.Optional;

/**
 * Use case for retrieving and rendering notification templates.
 *
 * Loads templates from the database and renders them with the provided payload.
 */
@Service
@Slf4j
public class GetTemplateUseCase {

    private final TemplateEntityRepository repository;

    public GetTemplateUseCase(TemplateEntityRepository repository) {
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

        Optional<TemplateEntity> entity = repository.findByIdAndActiveTrue(templateId);

        if (entity.isEmpty()) {
            log.warn("[NOT_FOUND] Template not found or inactive: templateId={}", templateId);
            throw new TemplateNotFoundException("Template not found: " + templateId);
        }

        TemplateEntity te = entity.get();

        Template template = new Template(
                te.getId(),
                te.getEventType(),
                te.getName(),
                te.getSubject(),
                te.getBody(),
                te.getVersion(),
                te.getActive(),
                te.getCreatedAt(),
                te.getUpdatedAt()
        );

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
