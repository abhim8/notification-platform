package notification.application.service;

import java.util.Optional;

/**
 * Port for resolving/rendering notification templates.
 *
 * Fetches template content by ID and renders it with the provided payload.
 * The actual implementation will call the template-service REST API.
 */
public interface TemplateResolver {

    /**
     * Resolve a template by its ID and render it with the given payload.
     *
     * @param templateId the unique template identifier
     * @param payload the dynamic data to inject into the template
     * @return the rendered template as a string
     * @throws TemplateResolutionException if template not found or rendering fails
     */
    String resolveTemplate(String templateId, java.util.Map<String, Object> payload);

    /**
     * Check if a template exists
     *
     * @param templateId the unique template identifier
     * @return true if template exists, false otherwise
     */
    boolean templateExists(String templateId);
}

