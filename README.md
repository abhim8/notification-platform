# Notification Platform

A production-grade, event-driven notification platform built with Java Spring Boot and Apache Kafka. Supports multi-channel delivery (email, SMS, push, webhook) with idempotency, retry logic, dead letter queues, and per-channel delivery tracking.

---

## Motivation

Most notification systems start simple and become a mess - scattered `sendEmail()` calls across services, no retry handling, no deduplication, and zero visibility into what actually got delivered. This project builds the right foundation: a dedicated notification service that any upstream service can publish to, with guaranteed delivery semantics and full observability.

---

## Architecture

```
Producers (Order / Auth / Alert / Promo services)
        │
        ▼
  Apache Kafka
  ├── notification.transactional   (6 partitions, 7d retention)
  ├── notification.marketing       (3 partitions, 3d retention)
  ├── notification.alerts          (6 partitions, 1d retention)
  ├── notification.retry           (3 partitions, 2d retention)
  └── notification.dlq             (3 partitions, 14d retention)
        │
        ▼
  Notification Service  ◄──►  Template Service
        │                            │
        ├── Email (SendGrid)             └── PostgreSQL (templates)
        ├── SMS (Twilio)
        ├── Push (Firebase FCM)
        └── Webhook (outbound HTTP)
        │
        ▼
  Delivery Tracker (PostgreSQL)
  Retry Scheduler (ShedLock + Redis)
  DLQ Consumer (exponential backoff)
```

**Key design decisions:**

- Partition key = `userId` - guarantees ordered delivery per user within a topic
- Idempotency key per event - deduplicated via Redis with a 24h window
- Failed deliveries retry 3 times (backoff: 1s -> 5s -> 30s), then route to DLQ
- `transactional` and `marketing` topics are separated so SLA differences can be enforced at the consumer group level
- Hexagonal architecture throughout - Kafka adapters, channel adapters, and infrastructure are all behind ports

---

## Tech stack

| Layer | Technology |
|---|---|
| Language | Java 23 |
| Framework | Spring Boot 3.x |
| Messaging | Apache Kafka |
| Cache / dedup | Redis |
| Database | PostgreSQL |
| Distributed lock | ShedLock |
| Email | SendGrid |
| SMS | Twilio |
| Push | Firebase FCM |
| Containerisation | Docker Compose |

---

## Project structure

```
notification-platform/
├── notification-service/
│   ├── domain/
│   │   ├── event/          # NotificationEvent, EventType
│   │   ├── channel/        # Channel, ChannelDispatcher port
│   │   └── model/          # DeliveryStatus, RetryPolicy
│   ├── application/
│   │   ├── usecase/        # SendNotificationUseCase, RetryUseCase
│   │   └── service/        # DeduplicationService, TemplateResolver
│   ├── adapter/
│   │   ├── kafka/          # KafkaConsumer, KafkaProducer (DLQ/retry)
│   │   └── channels/
│   │       ├── email/      # SendGridAdapter
│   │       ├── sms/        # TwilioAdapter
│   │       ├── push/       # FcmAdapter
│   │       └── webhook/    # WebhookAdapter
│   └── infrastructure/
│       ├── redis/          # IdempotencyStore
│       ├── postgres/       # DeliveryRepository
│       └── shedlock/       # RetrySchedulerConfig
├── template-service/
│   ├── domain/
│   ├── application/
│   └── adapter/
├── delivery-tracker/
│   ├── domain/
│   └── adapter/
└── docker-compose.yml
```

---

## Kafka event schema

Every upstream service publishes events in this shape:

```json
{
  "eventId":    "evt_01HXYZ9...",
  "eventType":  "ORDER_PLACED",
  "userId":     "usr_abc123",
  "channels":   ["email", "push"],
  "templateId": "order-confirm-v2",
  "payload": {
    "orderId": "ord_789",
    "amount": 99.00
  }
}
```

`eventId` is the idempotency key. If the same `eventId` is consumed twice within 24 hours, the second delivery is silently dropped.

---

## Retry and failure handling

```
Delivery attempt
      │
   success --> mark DELIVERED in tracker
      │
   failure
      │
   retry count < 3 --> publish to notification.retry (with backoff header)
      │
   retry count == 3 --> publish to notification.dlq
      │
   DLQ consumer --> alert + manual inspection dashboard (planned)
```

The retry scheduler runs every 30 seconds, protected by ShedLock against duplicate execution across instances.

---

## Running locally

**Prerequisites:** Java 23, Docker

```bash
git clone https://github.com/abhim8/notification-platform.git
cd notification-platform
docker-compose up -d        # starts Kafka, Redis, PostgreSQL
./mvnw spring-boot:run -pl notification-service
```

Environment variables required (copy `.env.example`):

```
SENDGRID_API_KEY=
FCM_SERVER_KEY=
REDIS_HOST=localhost
POSTGRES_URL=jdbc:postgresql://localhost:5432/notifications
```

---

## API

The notification service exposes a REST API for manual triggering and status checks:

```
POST /api/v1/notifications          # publish a notification event directly
GET  /api/v1/notifications/{id}     # get delivery status
GET  /api/v1/notifications/{id}/attempts  # get all delivery attempts per channel
```

Full OpenAPI spec available at `http://localhost:8080/swagger-ui.html` when running locally.

---

## What I'd add with more time

- Admin dashboard for DLQ inspection and manual replay
- Per-channel rate limiting (e.g. max 3 SMS/user/hour)
- Priority queue support within transactional topic
- Metrics via Micrometer + Grafana dashboard

---

## Blog post

[How I built a production-grade notification platform with Kafka](./BLOG.md) - covers the failure handling design, idempotency decisions, and lessons from the ShedLock + Redis setup.