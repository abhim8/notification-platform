package template.domain;

import java.time.LocalDateTime;

/**
 * Domain model representing a notification template.
 *
 * Templates are reusable content blueprints for specific event types.
 * They support placeholder substitution (e.g., {{userId}}, {{orderId}}).
 */
public record Template(
    String id,
    String eventType,
    String name,
    String subject,
    String body,
    int version,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

    /**
     * Render the template body with the given payload
     */
    public String renderBody(java.util.Map<String, Object> payload) {
        String rendered = body;
        for (java.util.Map.Entry<String, Object> entry : payload.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            rendered = rendered.replace(placeholder, value);
        }
        return rendered;
    }

    /**
     * Render the template subject with the given payload
     */
    public String renderSubject(java.util.Map<String, Object> payload) {
        if (subject == null) return null;
        String rendered = subject;
        for (java.util.Map.Entry<String, Object> entry : payload.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            rendered = rendered.replace(placeholder, value);
        }
        return rendered;
    }
}

