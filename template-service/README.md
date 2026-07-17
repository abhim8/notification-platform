# Template Service

[![Java](https://img.shields.io/badge/Java-23-blue?logo=openjdk)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-brightgreen?logo=spring)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-316192?logo=postgresql)](https://www.postgresql.org/)

> Centralized service for managing and rendering notification templates. Provides a template repository with dynamic content interpolation for multi-channel delivery.

**Port:** `8002` | **Navigation:** [Root](../README.md) · [Notification Service](../notification-service/README.md) · [Delivery Tracker](../delivery-tracker/README.md) · [Common](../notification-platform-common/README.md)

---

## Project Overview

The Template Service provides a centralized repository for notification templates. It stores templates in PostgreSQL and exposes REST endpoints for retrieval, existence checks, and dynamic rendering with user-provided data payloads. The Notification Service calls this service during event processing to resolve and render notification content before dispatch.

### Responsibilities

| Responsibility | Description |
|----------------|-------------|
| Template Storage | Persists notification templates in PostgreSQL |
| Template Retrieval | Provides APIs to fetch templates by ID or list all |
| Template Rendering | Dynamically interpolates template content with provided payload data |
| Existence Checks | Supports HEAD requests to verify template availability |

### Features

- **Centralized template repository** - single source of truth for all notification content
- **Dynamic rendering** - payload-based content interpolation for personalized notifications
- **Template versioning** - supports version tracking with active/inactive status
- **Event-type association** - templates linked to `EventType` for contextual retrieval
- **Liquibase migrations** - seeded with sample templates on startup

---

## Package Structure

```
template-service/
└── src/main/java/template/
    ├── TemplateApplication.java
    ├── domain/
    │   ├── Template.java                  # Domain model (ID, name, subject, body, etc.)
    │   └── TemplateRepository.java        # Port interface for persistence
    ├── application/
    │   ├── TemplateUseCase.java           # Business logic for template operations
    │   └── TemplateNotFoundException.java # Domain exception
    └── adapter/
        ├── postgres/
        │   ├── TemplatePersistenceAdapter.java  # Repository implementation
        │   ├── entity/TemplateEntity.java       # JPA entity
        │   └── repository/TemplateEntityRepository.java  # Spring Data JPA
        └── rest/
            ├── TemplateController.java    # REST endpoints
            ├── config/SwaggerConfig.java  # OpenAPI configuration
            └── dto/TemplateResponse.java  # Response DTO
```

---

## APIs

| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/v1/templates` | List all available templates |
| GET | `/api/v1/templates/{templateId}` | Get template by ID |
| POST | `/api/v1/templates/{templateId}/render` | Render template with JSON payload |
| HEAD | `/api/v1/templates/{templateId}` | Check if template exists (200=found, 404=not found) |

### Example: Render a Template

```bash
curl -X POST http://localhost:8002/api/v1/templates/order-confirm/render \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ord_789",
    "amount": "99.00"
  }'
```

### Example: Check Template Existence

```bash
curl -I http://localhost:8002/api/v1/templates/order-confirm
```

**Swagger UI:** `http://localhost:8002/swagger-ui.html`

---

## Database

| Aspect | Detail |
|--------|--------|
| Engine | PostgreSQL 16 |
| Schema | `notification_schema` |
| Entity | `TemplateEntity` - fields: `id`, `eventType` (enum), `name`, `subject`, `body`, `version`, `active`, `createdAt`, `updatedAt` |
| Repository | Spring Data JPA (`TemplateEntityRepository`) |
| Migrations | Liquibase (`v1__create_templates.xml`, `v2__insert_templates.xml`) |
| JPA Config | `ddl-auto: validate` (schema managed by Liquibase) |

### Template Rendering Flow

1. Notification Service calls `POST /api/v1/templates/{templateId}/render`
2. `TemplateController` delegates to `TemplateUseCase`
3. `TemplateUseCase` retrieves the active `Template` via `TemplateRepository` → `TemplatePersistenceAdapter` → `TemplateEntityRepository`
4. `TemplateUseCase` calls `Template.renderBody(payload)` for content interpolation
5. Rendered content returned as response

---

## Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `TEMPLATE_SERVICE_PORT` | `8002` | HTTP server port |
| `DB_HOST` | `jdbc:postgresql://localhost:5432/notification?currentSchema=notification_schema` | PostgreSQL JDBC URL |
| `DB_USER` | `notif_user` | Database username |
| `DB_PASSWORD` | (empty) | Database password |

---

## Running Locally

### Prerequisites

- **PostgreSQL 16** (port 5432) - `brew services start postgresql`
- Schema `notification_schema` must exist

### Start the Service

```bash
# From the project root
mvn spring-boot:run -pl template-service
```

### Build & Test

```bash
mvn clean compile -pl template-service
mvn spring-boot:run -pl template-service
```

> The Template Service does NOT require Kafka or Redis. PostgreSQL is the only dependency.

---

## Docker

The service does not have a standalone Dockerfile. Infrastructure dependencies are provided via the root [`docker-compose.yml`](../docker-compose.yml).

---

## Technology Stack

| Category | Technology | Purpose |
|----------|-----------|---------|
| Framework | Spring Boot 3.3.0 | Application framework |
| Database | PostgreSQL 16 | Template persistence |
| ORM | Spring Data JPA | Data access |
| Migrations | Liquibase 4.31.1 | Schema management |
| API Docs | Springdoc OpenAPI 2.1.0 | Swagger UI |
| Build | Maven | Build & dependencies |

---

## Documentation

| Document | Description |
|----------|-------------|
| [**Root README**](../README.md) | Platform overview, architecture, setup |
| [**Notification Service**](../notification-service/README.md) | Core orchestration service |
| [**Delivery Tracker**](../delivery-tracker/README.md) | Delivery attempt tracking |
| [**Common Module**](../notification-platform-common/README.md) | Shared domain and infrastructure |
| [**Startup Guide**](../STARTUP_GUIDE.md) | End-to-end local setup |
| [**Contributing**](../CONTRIBUTING.md) | Contribution guidelines |
| [**Security**](../SECURITY.md) | Security policy |
