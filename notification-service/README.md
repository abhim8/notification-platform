# Notification Service Module

This is the core orchestration service of the Notification Platform. It's responsible for consuming notification events, managing idempotency, dispatching messages through various channels, tracking delivery attempts, and handling retry logic for failed notifications.

## Purpose and Responsibilities

*   **Event Consumption:** Listens to Kafka topics for incoming notification requests.
*   **Deduplication:** Prevents duplicate processing of events using Redis.
*   **Template Resolution:** Interacts with the `template-service` to fetch and render notification content.
*   **Channel Dispatching:** Routes notifications to the appropriate channel adapters (email, SMS, push, webhook).
*   **Delivery Tracking:** Records all delivery attempts by calling the `delivery-tracker` service.
*   **Retry Management:** Schedules and re-processes failed notifications with configurable backoff policies using ShedLock.
*   **Error Handling:** Manages failures during dispatch and routes exhausted events to a Dead Letter Queue (DLQ).

## Public APIs

The Notification Service exposes a REST API for initiating notifications and querying high-level status:

*   **`POST /api/v1/notifications`**: Sends a new notification.
    *   **Request Body:** `notification.adapter.rest.dto.NotificationRequest`
    *   **Response:** `notification.adapter.rest.dto.NotificationResponse`
*   **`GET /api/v1/notifications/{eventId}`**: Retrieves high-level delivery status for an event. (Note: For detailed delivery attempt history, query the `delivery-tracker` service directly.)
*   **`GET /api/v1/notifications/{eventId}/attempts`**: Provides a link to the `delivery-tracker` service for detailed delivery attempts for a given event ID.

**Swagger Documentation:** `http://localhost:8001/swagger-ui.html`

## Kafka Consumers/Producers

The service interacts with Kafka for event processing and inter-service communication.

### Consumed Topics

| Topic                      | Consumer Group  | Purpose                                   | Partitions | Retention |
| :------------------------- | :-------------- | :---------------------------------------- | :--------- | :-------- |
| `notification.transactional` | `notif-svc-trans` | High-priority transactional notifications | 1          | 24 hours  |
| `notification.marketing`     | `notif-svc-mktg`  | Marketing-related notifications           | 1          | 24 hours  |
| `notification.alerts`        | `notif-svc-alerts`| Urgent alerts and critical notifications  | 1          | 24 hours  |
| `notification.retry`         | `notif-svc-retry` | Internal topic for failed events awaiting retry | 1          | 24 hours  |

### Produced Topics

| Topic                 | Purpose                                   |
| :-------------------- | :---------------------------------------- |
| `notification.retry`  | Events that failed initial dispatch and require re-processing. |
| `notification.dlq`    | Events that have exhausted all retry attempts. |

## Redis Usage

*   **Deduplication:** Redis is used to store `eventId`s for a configurable TTL (default 24 hours) to prevent processing duplicate notification requests. This ensures idempotency for incoming events.
    *   **Key Pattern:** `dedup:event:{eventId}`

## Retry Scheduler

The service includes a distributed retry scheduler powered by [ShedLock](https://github.com/lukas-krecan/ShedLock).

*   **Mechanism:** Periodically queries the `delivery-tracker` service for failed delivery attempts.
*   **Locking:** Uses ShedLock with PostgreSQL as the lock provider to ensure that only one instance of the `notification-service` runs the retry task in a clustered environment.
*   **Configuration:**
    *   `retry.scheduler.interval-ms`: Frequency of the scheduler run (default: 60000ms, configurable via `RETRY_SCHEDULER_INTERVAL_MS`).
    *   `retry.scheduler.initial-delay-ms`: Initial delay before the first run (default: 30000ms, configurable via `RETRY_SCHEDULER_INITIAL_DELAY_MS`).
*   **Retry Policy:**
    *   **Max Retries:** 3 attempts
    *   **Backoff Schedule**: Configured for exponential backoff (1s, 5s, 30s after failure, before being published to DLQ)
    *   **Exhaustion:** Events that exhaust all retries are published to the `notification.dlq` Kafka topic.

## Downstream Dependencies

*   **Template Service:** Calls the `template-service` (via `TemplateServiceClient`) to resolve and render notification templates. Configured via `template-service.base-url`.
*   **Delivery Tracker:** Calls the `delivery-tracker` service (via `DeliveryTrackerClient`) to record new delivery attempts and to retrieve failed attempts for retry processing. Configured via `delivery-tracker.base-url`.
*   **External Channels (Mocked):**
    *   **SendGrid:** Email dispatch (mocked).
    *   **Twilio:** SMS dispatch (mocked).
    *   **Firebase Cloud Messaging (FCM):** Push notification dispatch (mocked).
    *   **Webhook:** Generic HTTP POST to an external endpoint (mocked).

## Configuration

Key configurations can be overridden via environment variables or `application.yml`. Refer to the root `README.md`'s "Important Environment Variables" section for a comprehensive list.

## How to Run Locally

Refer to the main `README.md` in the project root for detailed instructions on setting up prerequisites (Docker, Kafka, PostgreSQL, Redis), building the application, and starting individual services.
