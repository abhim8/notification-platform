# Notification Platform - Setup & Startup Guide

## Architecture Overview

This is a production-grade, event-driven notification platform built with:
- **Java 23 + Spring Boot 3.x**
- **Apache Kafka** for event streaming
- **PostgreSQL** for persistent storage
- **Redis** for idempotency deduplication
- **ShedLock** for distributed retry scheduling
- **Multi-service** architecture with clear hexagonal layer boundaries

### Services

| Service | Port | Purpose |
|---------|------|---------|
| **notification-service** | 8001 | Core service: Kafka consumer, channel dispatcher, retry logic |
| **template-service** | 8002 | Template management and rendering |
| **delivery-tracker** | 8003 | Delivery attempt tracking and history |

### Tech Stack

- Kafka (5 topics with specific retention policies)
- PostgreSQL (templates, delivery attempts, ShedLock)
- Redis (event deduplication with 24h TTL)
- SendGrid, Twilio, Firebase, Webhook (all mocked)
- ShedLock (distributed scheduler)
- Swagger/OpenAPI (full documentation)

---

## Prerequisites

### System Requirements

- **Java 23** (or later)
- **Maven 3.8+**
- **Docker & Docker Compose**
- **PostgreSQL 16** (running locally on port 5432)
- **Redis 7.2** (running locally on port 6379)

### Local Services Setup

Before starting the application, ensure PostgreSQL and Redis are running locally:

```bash
# Start PostgreSQL locally (if not already running)
# On macOS with Homebrew:
brew services start postgresql

# Start Redis locally (if not already running)
# On macOS with Homebrew:
brew services start redis

# Verify connections:
psql -U notif_user -d notification_db  # Should connect
redis-cli ping                          # Should return "PONG"
```

---

## Quick Start

### Step 1: Start Kafka & Zookeeper

```bash
cd /Users/abhilash/IdeaProjects/notification-platform

# Start Kafka and Zookeeper (Docker Compose creates all 5 topics automatically)
docker-compose up -d

# Verify Kafka is running
docker-compose logs kafka-init
# Should see: "All topics created successfully!"
```

### Step 2: Initialize Database Schema

```bash
# Connect to PostgreSQL and run init script
psql -U notif_user -d notification_db -f init-db.sql

# Verify tables were created:
psql -U notif_user -d notification_db -c "\dt"
# Should see: delivery_attempts, shedlock, etc.
```

### Step 3: Build the Application

```bash
# Compile all modules
mvn clean compile

# Optionally run tests
mvn test

# Build JARs
mvn package
```

### Step 4: Start All Three Services

Each service runs in its own terminal:

**Terminal 1 - Notification Service (port 8001):**
```bash
cd /Users/abhilash/IdeaProjects/notification-platform
mvn spring-boot:run -pl notification-service
```

**Terminal 2 - Template Service (port 8002):**
```bash
cd /Users/abhilash/IdeaProjects/notification-platform
mvn spring-boot:run -pl template-service
```

**Terminal 3 - Delivery Tracker (port 8003):**
```bash
cd /Users/abhilash/IdeaProjects/notification-platform
mvn spring-boot:run -pl delivery-tracker
```

---

## API Usage

### Swagger Documentation

- **notification-service**: http://localhost:8001/swagger-ui.html
- **template-service**: http://localhost:8002/swagger-ui.html
- **delivery-tracker**: http://localhost:8003/swagger-ui.html

### Example 1: Send a Notification

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

**Expected Response:**
```json
{
  "eventId": "evt_001",
  "success": true,
  "status": "success",
  "message": "Notification sent successfully",
  "channelResults": {
    "email": {
      "success": true,
      "messageId": "sendgrid_mock_evt_001_...",
      "errorMessage": null
    },
    "sms": {
      "success": true,
      "messageId": "twilio_mock_evt_001_...",
      "errorMessage": null
    }
  }
}
```

### Example 2: Get Delivery Attempts

```bash
# Query delivery attempts for an event
curl http://localhost:8003/api/v1/delivery-attempts/events/evt_001

# Query by channel
curl http://localhost:8003/api/v1/delivery-attempts/events/evt_001/channels/email

# Query user's recent attempts
curl http://localhost:8003/api/v1/delivery-attempts/users/usr_123
```

### Example 3: Check Template

```bash
# Get a template
curl http://localhost:8002/api/v1/templates/order-confirm

# Render a template with data
curl -X POST http://localhost:8002/api/v1/templates/order-confirm/render \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ord_789",
    "amount": "99.00"
  }'
```

---

## Kafka Topics Reference

| Topic | Partitions | Retention | Consumer Group |
|-------|-----------|-----------|---|
| notification.transactional | 6 | 7 days | notif-svc-trans |
| notification.marketing | 3 | 3 days | notif-svc-mktg |
| notification.alerts | 6 | 1 day | notif-svc-alerts |
| notification.retry | 3 | 2 days | (retry logic) |
| notification.dlq | 3 | 14 days | (monitoring) |

### Monitor Kafka Topics

```bash
# List all topics
docker-compose exec kafka kafka-topics --list --bootstrap-server kafka:29092

# View messages in a topic
docker-compose exec kafka kafka-console-consumer \
  --bootstrap-server kafka:29092 \
  --topic notification.transactional \
  --from-beginning

# View topic details
docker-compose exec kafka kafka-topics --describe \
  --bootstrap-server kafka:29092 \
  --topic notification.transactional
```

---

## Event Idempotency & Retry Logic

### Deduplication (24-hour window)

- Events with the same `eventId` arriving within 24 hours are deduplicated
- Deduplication keys stored in Redis with TTL of 24 hours
- If duplicate detected: logged and dropped (no channels notified)

### Retry Policy

- **Max Retries**: 3 attempts
- **Backoff Schedule**:
  - Retry 1: 1 second after failure
  - Retry 2: 5 seconds after failure
  - Retry 3: 30 seconds after failure
- **After Exhaustion**: Event published to DLQ topic for monitoring
- **Scheduler**: Runs every 30 seconds, protected by ShedLock for distributed deployments

---

## Logging & Debugging

### Log Levels

- **Root**: INFO
- **notification**: DEBUG
- **template**: DEBUG
- **delivery**: DEBUG

### Key Log Patterns

```
[CONSUME] Message received
[PROCESS] Event processing started
[MOCK][EMAIL] Would send email (mock operation)
[MOCK][SMS] Would send SMS (mock operation)
[MOCK][PUSH] Would send push (mock operation)
[WEBHOOK] Posting to webhook
[DEDUP] Event found in dedup cache
[RETRY] Starting retry scheduler
[DLQ] Event exhausted retries
[SUCCESS] Event dispatched successfully
[ERROR] Operation failed
```

### Check Redis Keys

```bash
# Connect to Redis
redis-cli

# List deduplication keys
KEYS dedup:event:*

# Check TTL for a key
TTL dedup:event:evt_001

# Get value
GET dedup:event:evt_001
```

---

## Configuration via Environment Variables

All services support environment variable overrides:

```bash
# Notification Service
export SERVER_PORT=8001
export KAFKA_BOOTSTRAP_SERVERS=localhost:9092
export DB_HOST=localhost:5432
export REDIS_HOST=localhost
export RETRY_SCHEDULER_INTERVAL_MS=30000

# Template Service
export TEMPLATE_SERVICE_PORT=8002
export DB_HOST=localhost:5432

# Delivery Tracker
export DELIVERY_TRACKER_PORT=8003
export DB_HOST=localhost:5432

# Channel Configuration
export SENDGRID_API_KEY=your-key-here
export SENDGRID_FROM_EMAIL=noreply@example.com
export TWILIO_ACCOUNT_SID=your-sid
export TWILIO_FROM_NUMBER=+1234567890
export FIREBASE_ENABLED=true
export FIREBASE_PROJECT_ID=your-project
```

---

## Troubleshooting

### Kafka Connection Issues

```bash
# Check if Kafka is running
docker-compose ps

# View Kafka logs
docker-compose logs -f kafka

# Verify topics exist
docker-compose exec kafka kafka-topics --list --bootstrap-server kafka:29092
```

### Database Connection Issues

```bash
# Test PostgreSQL connection
psql -U notif_user -d notification_db -c "SELECT 1"

# Check tables
psql -U notif_user -d notification_db -c "\dt"

# View table structure
psql -U notif_user -d notification_db -c "\d delivery_attempts"
```

### Redis Connection Issues

```bash
# Test Redis
redis-cli ping

# Check keys
redis-cli KEYS "*"

# Flush dedup cache (development only)
redis-cli FLUSHDB
```

### Application Startup Issues

```bash
# Build with full output
mvn clean compile -e

# Run with debug logging
mvn spring-boot:run -pl notification-service -Dspring-boot.run.arguments="--debug"

# Check logs in target/
tail -f notification-service/target/*.log
```

---

## Clean Shutdown

```bash
# Stop all services
docker-compose down

# Stop local PostgreSQL
brew services stop postgresql

# Stop local Redis
brew services stop redis

# Clean build artifacts
mvn clean
```

---

## Architecture Layers

### Domain Layer (No Infrastructure Dependencies)
- `domain/event/` - EventType, NotificationEvent
- `domain/channel/` - Channel, ChannelDispatcher (port), DispatchResult
- `domain/model/` - DeliveryStatus, RetryPolicy

### Application Layer (Business Logic)
- `application/service/` - DeduplicationService (port), TemplateResolver (port)
- `application/usecase/` - SendNotificationUseCase, RetryUseCase

### Adapter Layer (External Integration)
- `adapter/kafka/` - KafkaConsumer, KafkaProducer
- `adapter/channels/` - SendGridAdapter, TwilioAdapter, FcmAdapter, WebhookAdapter
- `adapter/rest/` - REST controllers
- `adapter/postgres/` - JPA entities, repositories

### Infrastructure Layer (External Services)
- `infrastructure/redis/` - RedisDeduplicationService
- `infrastructure/postgres/` - PostgresDeliveryAttemptRecorder
- `infrastructure/shedlock/` - RetryScheduler
- `infrastructure/template/` - MockTemplateResolver

---

## Next Steps

1. **Add Real Channel Implementations**
   - Replace mock SendGrid/Twilio/Firebase calls with actual API calls
   - Update to call real template-service instead of MockTemplateResolver

2. **Implement Delivery Status Tracking**
   - Create EventStore to track overall notification status
   - Add dashboard to visualize delivery metrics

3. **Add Monitoring & Alerts**
   - Set up Prometheus metrics
   - Create Grafana dashboards
   - Configure alerting for failures

4. **Production Deployment**
   - Move to Kubernetes
   - Add distributed tracing (Jaeger)
   - Implement distributed Circuit Breaker (Resilience4j)
   - Add data encryption

---

## Support

For issues or questions about the notification platform, refer to:
- API Documentation: http://localhost:PORT/swagger-ui.html
- Logs: target/notification-service.log (or relevant service)
- Database Schema: init-db.sql
- Docker Compose: docker-compose.yml

