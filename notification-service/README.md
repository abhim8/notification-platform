# Notification Service

[![Java](https://img.shields.io/badge/Java-23-blue?logo=openjdk)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-brightgreen?logo=spring)](https://spring.io/projects/spring-boot)
[![Apache Kafka](https://img.shields.io/badge/Apache%20Kafka-7.5.0-red?logo=apache-kafka)](https://kafka.apache.org/)
[![Redis](https://img.shields.io/badge/Redis-7.2-DC382D?logo=redis)](https://redis.io/)

> Core orchestration service for the Notification Platform. Consumes events, dispatches through channels, manages idempotency, and handles retry logic.

**Port:** `8001` | **Navigation:** [Root](../README.md) · [Template Service](../template-service/README.md) · [Delivery Tracker](../delivery-tracker/README.md) · [Common](../notification-platform-common/README.md)

---

## Project Overview

The Notification Service is the central orchestrator of the platform. It consumes notification events from Kafka, performs deduplication via Redis, resolves templates through the Template Service, dispatches messages through channel-specific adapters, records delivery attempts via the Delivery Tracker, and manages retry scheduling for failed deliveries.

### Responsibilities

| Responsibility | Description |
|----------------|-------------|
| Event Consumption | Listens to Kafka topics for incoming notification requests |
| Deduplication | Prevents duplicate event processing using Redis (24h TTL) |
| Template Resolution | Fetches and renders notification content from the Template Service |
| Channel Dispatching | Routes notifications to email, SMS, push, and webhook adapters |
| Delivery Tracking | Records all delivery attempts via the Delivery Tracker API |
| Retry Management | Schedules re-processing of failed notifications with exponential backoff |
| Dead Letter Queue | Publishes exhausted events to `notification.dlq` for monitoring |

### Features

- **Kafka-driven** async processing with topic segregation (transactional, marketing, alerts)
- **Redis-backed idempotency** - event deduplication with configurable TTL
- **Multi-channel dispatch** - SendGrid (email), Twilio (SMS), FCM (push), Webhook (HTTP POST)
- **Distributed retry scheduler** - ShedLock-based, single-instance execution in clustered deployments
- **Exponential backoff** - 1s → 5s → 30s between retry attempts
- **MDC trace propagation** - `traceId` flows across Kafka and REST boundaries
- **Structured JSON logging** - Log4j2 with JSON template layout

---

## Package Structure

```
notification-service/
└── src/main/java/notification/
    ├── NotificationServiceApplication.java
    ├── domain/
    │   ├── channel/
    │   │   ├── ChannelDispatcher.java          # Port interface for channel dispatch
    │   │   └── DispatchResult.java             # Dispatch outcome model
    │   ├── event/
    │   │   └── NotificationEvent.java          # Kafka event payload
    │   └── model/
    │       ├── NotificationStatus.java         # Notification lifecycle status
    │       └── RetryPolicy.java                # Retry configuration model
    ├── application/
    │   ├── service/
    │   │   ├── DeduplicationService.java       # Port for dedup logic
    │   │   ├── DeliveryAttemptRecorder.java    # Port for recording attempts
    │   │   ├── FailedDeliveryLoader.java       # Port for loading failures
    │   │   └── TemplateResolver.java           # Port for template resolution
    │   └── usecase/
    │       ├── SendNotificationUseCase.java    # Core notification flow
    │       ├── RetryUseCase.java               # Retry processing flow
    │       ├── SendNotificationResult.java     # Use case result model
    │       └── DeliveryAttemptCommand.java     # Command model for attempts
    ├── adapter/
    │   ├── channels/
    │   │   ├── config/ChannelDispatcherConfig.java
    │   │   ├── email/SendGridAdapter.java      # Email channel (mocked)
    │   │   ├── sms/TwilioAdapter.java          # SMS channel (mocked)
    │   │   ├── push/FcmAdapter.java            # Push channel (mocked)
    │   │   └── webhook/WebhookAdapter.java     # Webhook channel (mocked)
    │   ├── kafka/
    │   │   ├── NotificationKafkaConsumer.java  # Kafka event consumer
    │   │   ├── NotificationKafkaProducer.java  # Kafka event producer
    │   │   └── config/KafkaConfig.java         # Kafka client configuration
    │   └── rest/
    │       ├── NotificationController.java     # REST endpoints
    │       ├── config/SwaggerConfig.java       # OpenAPI configuration
    │       └── dto/
    │           ├── NotificationRequest.java    # Incoming request DTO
    │           └── NotificationResponse.java   # Outgoing response DTO
    └── infrastructure/
        ├── client/
        │   ├── DeliveryTrackerClient.java      # HTTP client for Delivery Tracker
        │   └── TemplateServiceClient.java      # HTTP client for Template Service
        ├── redis/
        │   ├── RedisDeduplicationService.java  # Redis-backed deduplication
        │   └── config/
        │       ├── RedisConfig.java            # Redis connection config
        │       └── RedisStartupCheck.java      # Startup health check
        ├── shedlock/
        │   ├── RetryScheduler.java             # Distributed retry scheduler
        │   └── config/ShedLockConfig.java      # ShedLock configuration
        ├── deliverytracker/
        │   ├── InMemoryDeliveryStore.java
        │   ├── MockDeliveryAttemptRecorder.java
        │   └── MockFailedDeliveryLoader.java
        └── template/
            └── MockTemplateResolver.java       # Mock template resolver
```

---

## APIs

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/notifications` | Submit a new notification for processing |
| GET | `/api/v1/notifications/{eventId}` | Get high-level delivery status for an event |
| GET | `/api/v1/notifications/{eventId}/attempts` | Redirect to Delivery Tracker for event attempts |

### Example: Send a Notification

```bash
curl -X POST http://localhost:8001/api/v1/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "eventId": "evt_001",
    "eventType": "ORDER_PLACED",
    "userId": "usr_123",
    "channels": ["email", "sms"],
    "templateId": "order-confirm",
    "payload": {
      "orderId": "ord_789",
      "amount": 99.00,
      "email_recipient": "user@example.com",
      "sms_recipient": "+1234567890"
    }
  }'
```

**Swagger UI:** `http://localhost:8001/swagger-ui.html`

---

## Kafka Topics

### Consumed Topics

| Topic | Consumer Group | Purpose |
|-------|---------------|---------|
| `notification.transactional` | `notif-svc-trans` | High-priority transactional notifications |
| `notification.marketing` | `notif-svc-mktg` | Marketing notifications |
| `notification.alerts` | `notif-svc-alerts` | Urgent alerts and critical notifications |
| `notification.retry` | `notif-svc-retry` | Failed events awaiting retry processing |

### Produced Topics

| Topic | Purpose |
|-------|---------|
| `notification.retry` | Events that failed initial dispatch and require re-processing |
| `notification.dlq` | Events that exhausted all retry attempts |

All topics: 1 partition, 24-hour retention.

---

## Redis Usage

Redis is used exclusively for **event deduplication** to ensure idempotency.

| Aspect | Detail |
|--------|--------|
| Key Pattern | `dedup:event:{eventId}` |
| TTL | 24 hours (configurable via `IDEMPOTENCY_TTL_HOURS`) |
| Behavior | Duplicate `eventId`s arriving within the TTL window are silently dropped |

```bash
# Inspect dedup keys
redis-cli KEYS "dedup:event:*"
redis-cli TTL dedup:event:evt_001
```

---

## Retry Scheduler

The service includes a distributed retry scheduler powered by [ShedLock](https://github.com/lukas-krecan/ShedLock) with PostgreSQL as the lock provider.

| Parameter | Default | Description |
|-----------|---------|-------------|
| `retry.scheduler.interval-ms` | 60000 | Scheduler execution frequency |
| `retry.scheduler.initial-delay-ms` | 30000 | Delay before first execution |

**Retry Policy:**

| Attempt | Backoff |
|---------|---------|
| 1st retry | 1 second |
| 2nd retry | 5 seconds |
| 3rd retry | 30 seconds |
| Exhaustion | Published to `notification.dlq` |

---

## Configuration

Key environment variables. See the root [README.md](../README.md#configuration) for the full list.

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | `8001` | HTTP server port |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker address |
| `DB_HOST` | *(see root README)* | PostgreSQL JDBC URL |
| `REDIS_HOST` | `localhost` | Redis host |
| `REDIS_PORT` | `6379` | Redis port |
| `TEMPLATE_SERVICE_URL` | `http://localhost:8002` | Template Service base URL |
| `DELIVERY_TRACKER_URL` | `http://localhost:8003` | Delivery Tracker base URL |
| `RETRY_SCHEDULER_INTERVAL_MS` | `60000` | Retry scheduler interval |
| `RETRY_SCHEDULER_INITIAL_DELAY_MS` | `30000` | Retry scheduler initial delay |
| `IDEMPOTENCY_TTL_HOURS` | `24` | Deduplication cache TTL |

### Channel Configuration

| Variable | Default | Description |
|----------|---------|-------------|
| `SENDGRID_API_KEY` | `mock-key` | SendGrid API key |
| `SENDGRID_FROM_EMAIL` | `noreply@notification-platform.com` | Sender email |
| `TWILIO_ACCOUNT_SID` | `mock-sid` | Twilio account SID |
| `TWILIO_AUTH_TOKEN` | `mock-token` | Twilio auth token |
| `TWILIO_FROM_NUMBER` | `+1234567890` | Twilio sender number |
| `FIREBASE_ENABLED` | `false` | Enable Firebase push |
| `FIREBASE_PROJECT_ID` | `mock-project` | Firebase project ID |
| `WEBHOOK_TIMEOUT_MS` | `5000` | Webhook timeout |
| `WEBHOOK_MAX_RETRIES` | `1` | Webhook max retries |

---

## Running Locally

### Prerequisites

Ensure the following are running:
- **PostgreSQL 16** (port 5432) - `brew services start postgresql`
- **Redis 7.2** (port 6379) - `brew services start redis`
- **Kafka + Zookeeper** - `docker-compose up -d` (from project root)

### Start the Service

```bash
# From the project root
mvn spring-boot:run -pl notification-service
```

### Build & Test

```bash
mvn clean test -pl notification-service
mvn spring-boot:run -pl notification-service
```

---

## Docker

The service does not have a standalone Dockerfile. Infrastructure dependencies (Kafka, Zookeeper, Kafka UI) are provided via the root [`docker-compose.yml`](../docker-compose.yml).

---

## Technology Stack

| Category | Technology | Purpose |
|----------|-----------|---------|
| Framework | Spring Boot 3.3.0 | Application framework |
| Event Streaming | Apache Kafka 7.5.0 (via `spring-kafka`) | Async event processing |
| Database | PostgreSQL 16 (via JDBC, no JPA) | ShedLock lock store |
| Cache | Redis 7.2 (via `spring-data-redis` + Jedis) | Event deduplication |
| Scheduling | ShedLock 5.9.1 | Distributed retry locking |
| Email | SendGrid SDK 4.10.2 | Email channel (mocked) |
| SMS | Twilio SDK 9.2.0 | SMS channel (mocked) |
| Push | Firebase Admin SDK 9.2.0 | Push channel (mocked) |
| API Docs | Springdoc OpenAPI 2.1.0 | Swagger UI |
| Logging | Log4j2 + JSON Template | Structured logging |
| Build | Maven | Build & dependencies |

---

## Downstream Dependencies

| Dependency | Protocol | Client Class | Configuration |
|-----------|----------|-------------|---------------|
| Template Service | REST (HTTP) | `TemplateServiceClient` | `template-service.base-url` |
| Delivery Tracker | REST (HTTP) | `DeliveryTrackerClient` | `delivery-tracker.base-url` |
| SendGrid (Email) | REST (HTTP) | `SendGridAdapter` | `sendgrid.*` (mocked) |
| Twilio (SMS) | REST (HTTP) | `TwilioAdapter` | `twilio.*` (mocked) |
| Firebase (Push) | REST (HTTP) | `FcmAdapter` | `firebase.*` (mocked, disabled) |
| Webhook | REST (HTTP) | `WebhookAdapter` | `webhook.*` |

---

## Documentation

| Document | Description |
|----------|-------------|
| [**Root README**](../README.md) | Platform overview, architecture, setup |
| [**Template Service**](../template-service/README.md) | Template management and rendering |
| [**Delivery Tracker**](../delivery-tracker/README.md) | Delivery attempt tracking |
| [**Common Module**](../notification-platform-common/README.md) | Shared domain and infrastructure |
| [**Startup Guide**](../STARTUP_GUIDE.md) | End-to-end local setup |
| [**Contributing**](../CONTRIBUTING.md) | Contribution guidelines |
| [**Security**](../SECURITY.md) | Security policy |
