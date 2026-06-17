package template.adapter.rest.dto;

import java.time.LocalDateTime;

/**
 * DTO for template REST API responses
 */
public record TemplateResponse(
    String id,
    String eventType,
    String name,
    String subject,
    String body,
    int version,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}

