package template.adapter.rest.dto;

import com.notification.common.domain.EventType;

import java.time.LocalDateTime;

public record TemplateResponse(
    String id,
    EventType eventType,
    String name,
    String subject,
    String body,
    int version,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
