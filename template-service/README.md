# Template Service Module

This service is dedicated to the management and rendering of notification templates. It provides a centralized repository for templates, allowing other services (like the Notification Service) to fetch and dynamically render content for various communication channels.

## Purpose and Responsibilities

*   **Template Storage:** Persists notification templates in a PostgreSQL database.
*   **Template Retrieval:** Provides APIs to fetch templates by ID.
*   **Template Rendering:** Dynamically renders template content (subject and body) using a provided data payload.
*   **Template Versioning:** Supports template versions, though the current implementation focuses on retrieving active templates.

## Public APIs

The Template Service exposes a REST API for template management and rendering:

*   **`GET /api/v1/templates`**: Retrieves all available templates.
    *   **Response:** `List<template.adapter.rest.dto.TemplateResponse>`
*   **`GET /api/v1/templates/{templateId}`**: Retrieves a specific template by its ID.
    *   **Response:** `template.adapter.rest.dto.TemplateResponse`
*   **`POST /api/v1/templates/{templateId}/render`**: Renders a template with the provided JSON payload.
    *   **Request Body:** `Map<String, Object>` (JSON payload for rendering)
    *   **Response:** `template.adapter.rest.dto.RenderResponse` (containing the rendered content)
*   **`HEAD /api/v1/templates/{templateId}`**: Checks if a template exists and is active. Returns HTTP 200 if found, 404 otherwise.

**Swagger Documentation:** `http://localhost:8002/swagger-ui.html`

## Database Usage

*   **PostgreSQL:** Used as the primary data store for `TemplateEntity` objects.
*   **`TemplateEntity`**: The JPA entity representing a notification template, including its ID, `EventType`, name, subject, body, version, active status, and timestamps.
    *   `@Enumerated(EnumType.STRING)` is used for `EventType` to ensure database compatibility with string values.
*   **`TemplateEntityRepository`**: Spring Data JPA repository for data access operations.

## Template Rendering Flow

1.  A request (e.g., from `notification-service`) calls `POST /api/v1/templates/{templateId}/render` with a `templateId` and a `payload`.
2.  The `TemplateController` delegates to `TemplateUseCase`.
3.  `TemplateUseCase` retrieves the active `Template` from the `TemplateRepository` (which uses `TemplatePersistenceAdapter` to interact with `TemplateEntityRepository`).
4.  If the template is found, `TemplateUseCase` calls `template.domain.Template.renderBody(payload)` to perform the actual rendering. The `Template` domain object is responsible for interpolating the `payload` data into its `body` field.
5.  The rendered string content is returned as `template.adapter.rest.dto.RenderResponse`.

## Configuration

Key configurations can be overridden via environment variables or `application.yml`. Refer to the root `README.md`'s "Important Environment Variables" section for a comprehensive list.

## How to Run Locally

Refer to the main `README.md` in the project root for detailed instructions on setting up prerequisites (Docker, Kafka, PostgreSQL, Redis), building the application, and starting individual services.
