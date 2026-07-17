# Delivery Tracker

[![Java](https://img.shields.io/badge/Java-23-blue?logo=openjdk)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-brightgreen?logo=spring)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-316192?logo=postgresql)](https://www.postgresql.org/)

> Dedicated service for recording and querying notification delivery attempts. Provides a persistent audit trail with support for event-, user-, and channel-based queries.

**Port:** `8003` | **Navigation:** [Root](../README.md) · [Notification Service](../notification-service/README.md) · [Template Service](../template-service/README.md) · [Common](../notification-platform-common/README.md)

---

## Project Overview

The Delivery Tracker is the persistence and query layer for all delivery attempt data. Every time a notification is dispatched through a channel, the result is recorded here. The service supports rich querying by event ID, user ID, channel, and time range, enabling audit, monitoring, and retry scheduling.

### Responsibilities

| Responsibility | Description |
|----------------|-------------|
| Delivery Recording | Persists all notification delivery attempts to PostgreSQL |
| History Queries | Provides APIs to query attempts by event, user, channel, and time range |
| Status Tracking | Tracks delivery lifecycle: PENDING → DELIVERED / FAILED / RETRYING / DLQ / DROPPED |
| Failed Attempt Detection | Identifies failed attempts for the Notification Service retry scheduler |
| Audit Trail | Maintains comprehensive history for compliance and debugging |

### Features

- **Full CRUD** for delivery attempt records via REST API
- **Multi-dimensional queries** - by event ID, user ID, channel, status, time range
- **Optimized indexes** - `event_id`, `user_id`, `channel`, `status`, `created_at`
- **Failed attempt filtering** - `since` and `limit` parameters for retry scheduler integration
- **Liquibase-managed schema** - automated database migrations
- **Hexagonal architecture** - clean separation of REST, persistence, and domain layers

---

## Package Structure

```
delivery-tracker/
└── src/main/java/delivery/
    ├── DeliveryTrackerApplication.java
    ├── application/
    │   ├── CreateAttemptCommand.java          # Command model
    │   └── DeliveryAttemptUseCase.java        # Business logic
    └── adapter/
        ├── postgres/
        │   ├── entity/DeliveryAttemptEntity.java        # JPA entity
        │   └── repository/DeliveryAttemptEntityRepository.java  # Spring Data JPA
        └── rest/
            ├── DeliveryAttemptController.java           # REST endpoints
            ├── config/SwaggerConfig.java                # OpenAPI configuration
            └── dto/
                ├── CreateDeliveryAttemptRequest.java    # Incoming request DTO
                └── DeliveryAttemptResponse.java         # Outgoing response DTO
```

---

## APIs

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/delivery-attempts` | Record a new delivery attempt (201 Created) |
| GET | `/api/v1/delivery-attempts/{attemptId}` | Get a single attempt by ID |
| GET | `/api/v1/delivery-attempts/events/{eventId}` | Get all attempts for an event |
| GET | `/api/v1/delivery-attempts/events/{eventId}/channels/{channel}` | Get attempts for event + channel |
| GET | `/api/v1/delivery-attempts/users/{userId}` | Get all attempts for a user |
| GET | `/api/v1/delivery-attempts/failed` | Get failed attempts (for retry scheduling) |

### Query Parameters for Failed Attempts

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `since` | ISO-8601 timestamp | - | Retrieve attempts since this time |
| `limit` | integer | 100 | Maximum number of results |

### Example: Record a Delivery Attempt

```bash
curl -X POST http://localhost:8003/api/v1/delivery-attempts \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": "evt_001",
    "userId": "usr_123",
    "eventType": "ORDER_PLACED",
    "channel": "EMAIL",
    "status": "DELIVERED",
    "attemptNumber": 1,
    "messageId": "sendgrid_20260617_xyz",
    "errorMessage": null
  }'
```

### Example: Query Attempts for an Event

```bash
curl http://localhost:8003/api/v1/delivery-attempts/events/evt_001
```

### Example: Query Failed Attempts for Retry

```bash
curl "http://localhost:8003/api/v1/delivery-attempts/failed?since=2026-06-17T10:00:00Z&limit=100"
```

### Example: Query User's Notifications

```bash
curl http://localhost:8003/api/v1/delivery-attempts/users/usr_123
```

**Swagger UI:** `http://localhost:8003/swagger-ui.html`

---

## Database

### Schema

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGINT (PK, auto) | Unique attempt identifier |
| `event_id` | VARCHAR(255) | Notification event reference |
| `user_id` | VARCHAR(255) | Recipient user reference |
| `event_type` | VARCHAR(50) | Event type enum string |
| `channel` | VARCHAR(50) | Notification channel (EMAIL, SMS, PUSH, WEBHOOK) |
| `status` | VARCHAR(50) | Delivery status enum string |
| `attempt_number` | INTEGER | Attempt sequence number |
| `message_id` | VARCHAR(255) | External provider message ID |
| `error_message` | TEXT | Error details (nullable) |
| `created_at` | TIMESTAMP | Record creation time |
| `updated_at` | TIMESTAMP | Last update time |

### Indexes

| Index | Columns | Purpose |
|-------|---------|---------|
| `idx_event_id` | `event_id` | Query attempts by event |
| `idx_user_id` | `user_id` | Query attempts by user |
| `idx_channel` | `channel` | Filter by channel |
| `idx_status` | `status` | Identify failed attempts |
| `idx_created_at` | `created_at` | Time-range queries |

### Migrations

Liquibase changelog: [`v1__create_delivery_attempts.xml`](src/main/resources/db/changelog/v1__create_delivery_attempts.xml)

---

## Delivery Attempt Statuses

| Status | Description |
|--------|-------------|
| `PENDING` | Awaiting processing |
| `DELIVERED` | Successfully delivered through the channel |
| `FAILED` | Delivery failed, will be retried |
| `RETRYING` | Being retried after a failure |
| `DLQ` | Exhausted all retries, moved to dead letter queue |
| `DROPPED` | Notification dropped (duplicate, unsupported channel, etc.) |

---

## Integration with Notification Service

The Delivery Tracker serves two primary functions for the Notification Service:

1. **Recording Delivery Attempts** - After dispatching a notification through a channel, the Notification Service calls `POST /api/v1/delivery-attempts` to persist the result.

2. **Retrieving Failed Attempts** - The Notification Service's retry scheduler calls `GET /api/v1/delivery-attempts/failed` to identify attempts that need to be retried.

---

## Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `DELIVERY_TRACKER_PORT` | `8003` | HTTP server port |
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
mvn spring-boot:run -pl delivery-tracker
```

### Build & Test

```bash
mvn clean compile -pl delivery-tracker
mvn spring-boot:run -pl delivery-tracker
```

> The Delivery Tracker does NOT require Kafka or Redis. PostgreSQL is the only dependency.

---

## Docker

The service does not have a standalone Dockerfile. Infrastructure dependencies are provided via the root [`docker-compose.yml`](../docker-compose.yml).

---

## Technology Stack

| Category | Technology | Purpose |
|----------|-----------|---------|
| Framework | Spring Boot 3.3.0 | Application framework |
| Database | PostgreSQL 16 | Delivery attempt persistence |
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
| [**Template Service**](../template-service/README.md) | Template management and rendering |
| [**Common Module**](../notification-platform-common/README.md) | Shared domain and infrastructure |
| [**Startup Guide**](../STARTUP_GUIDE.md) | End-to-end local setup |
| [**Contributing**](../CONTRIBUTING.md) | Contribution guidelines |
| [**Security**](../SECURITY.md) | Security policy |
