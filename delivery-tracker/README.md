# Delivery Tracker Service

This service is dedicated to recording and querying the history of all notification delivery attempts. It provides a persistent audit trail of how and when notifications were sent across all channels, supporting efficient queries by event ID, user ID, channel, and time range.

## Purpose and Responsibilities

*   **Delivery Attempt Recording:** Persists all notification delivery attempts to a PostgreSQL database.
*   **Delivery History Retrieval:** Provides APIs to query delivery attempt history by various criteria (event ID, user ID, channel, time range).
*   **Status Tracking:** Tracks the status of each delivery attempt (PENDING, DELIVERED, FAILED, RETRYING, DLQ, DROPPED).
*   **Failed Attempt Identification:** Identifies failed attempts for the retry scheduler in the notification service.
*   **Audit Trail:** Maintains a comprehensive history of all delivery operations for auditing and compliance purposes.

## Public APIs

The Delivery Tracker Service exposes a REST API for recording and querying delivery attempts:

*   **`POST /api/v1/delivery-attempts`**: Records a new delivery attempt.
    *   **Request Body:** `delivery.adapter.rest.dto.DeliveryAttemptRequest`
    *   **Response:** `delivery.adapter.rest.dto.DeliveryAttemptResponse` (HTTP 201 Created)
    *   **Example Payload:**
    ```json
    {
      "eventId": "evt_001",
      "userId": "usr_123",
      "eventType": "ORDER_PLACED",
      "channel": "EMAIL",
      "status": "DELIVERED",
      "attemptNumber": 1,
      "messageId": "sendgrid_msg_789",
      "errorMessage": null
    }
    ```

*   **`GET /api/v1/delivery-attempts/{attemptId}`**: Retrieves a single delivery attempt by ID.
    *   **Response:** `delivery.adapter.rest.dto.DeliveryAttemptResponse`

*   **`GET /api/v1/delivery-attempts/events/{eventId}`**: Retrieves all delivery attempts for a specific event.
    *   **Response:** `List<DeliveryAttemptResponse>`
    *   **Example:** `GET /api/v1/delivery-attempts/events/evt_001`

*   **`GET /api/v1/delivery-attempts/events/{eventId}/channels/{channel}`**: Retrieves delivery attempts for a specific event and channel.
    *   **Response:** `List<DeliveryAttemptResponse>`
    *   **Example:** `GET /api/v1/delivery-attempts/events/evt_001/channels/EMAIL`

*   **`GET /api/v1/delivery-attempts/users/{userId}`**: Retrieves all delivery attempts for a specific user.
    *   **Response:** `List<DeliveryAttemptResponse>`
    *   **Example:** `GET /api/v1/delivery-attempts/users/usr_123`

*   **`GET /api/v1/delivery-attempts/failed`**: Retrieves failed delivery attempts for retry processing.
    *   **Query Parameters:**
        *   `since` (ISO-8601 timestamp): Retrieve attempts since this time
        *   `limit` (integer, default 100): Maximum number of results
    *   **Response:** `List<DeliveryAttemptResponse>`
    *   **Example:** `GET /api/v1/delivery-attempts/failed?since=2026-06-17T10:00:00Z&limit=50`

**Swagger Documentation:** `http://localhost:8003/swagger-ui.html`

## Database Usage

*   **PostgreSQL:** Stores all delivery attempts with full audit trail.
*   **`DeliveryAttemptEntity`**: The JPA entity representing a single delivery attempt.
    *   **Fields:**
        *   `id` (Long): Primary key, auto-generated
        *   `eventId` (String): Reference to the notification event
        *   `userId` (String): Reference to the recipient user
        *   `eventType` (Enum): Type of event that triggered the notification
        *   `channel` (Enum): Notification channel (EMAIL, SMS, PUSH, WEBHOOK)
        *   `status` (Enum): Current status of the delivery attempt
        *   `attemptNumber` (Integer): Which attempt number this is (1, 2, 3, etc.)
        *   `messageId` (String): External message ID from the channel provider
        *   `errorMessage` (String): Error details if delivery failed
        *   `createdAt` (LocalDateTime): When the attempt was recorded
        *   `updatedAt` (LocalDateTime): When the record was last updated

*   **Indexes:**
    *   `idx_event_id`: Enables efficient queries by event ID
    *   `idx_user_id`: Enables efficient queries by user ID
    *   `idx_channel`: Enables efficient filtering by channel
    *   `idx_status`: Enables efficient identification of failed attempts
    *   `idx_created_at`: Enables time-range queries and pagination

## Delivery Attempt Statuses

*   **PENDING**: Delivery attempt is waiting to be processed
*   **DELIVERED**: Notification was successfully delivered through the channel
*   **FAILED**: Delivery attempt failed and will be retried
*   **RETRYING**: Attempt is being retried after an earlier failure
*   **DLQ**: Delivery exhausted all retries and moved to Dead Letter Queue
*   **DROPPED**: Notification was dropped (e.g., duplicate, unsupported channel)

## Integration with Notification Service

The Delivery Tracker serves two primary functions for the notification service:

1. **Recording Delivery Attempts**: After dispatching a notification through a channel, the notification service calls `POST /api/v1/delivery-attempts` to record the result.

2. **Retrieving Failed Attempts**: The notification service's retry scheduler calls `GET /api/v1/delivery-attempts/failed` to identify attempts that need to be retried.

## Configuration

Key configurations can be overridden via environment variables or `application.yml`:

| Configuration | Environment Variable | Default | Purpose |
|---|---|---|---|
| Server Port | `DELIVERY_TRACKER_PORT` | 8003 | HTTP port for the service |
| Database URL | `DB_HOST` | `jdbc:postgresql://localhost:5432/notification?currentSchema=notification_schema` | PostgreSQL connection string |
| Database User | `DB_USER` | `abhilash` | PostgreSQL username |
| Database Password | `DB_PASSWORD` | (empty) | PostgreSQL password |

Refer to the root `README.md` for a comprehensive list of all environment variables.

## How to Run Locally

Refer to the main `README.md` in the project root for detailed instructions on setting up prerequisites (Docker, Kafka, PostgreSQL, Redis), building the application, and starting individual services.

## Example Usage

### Record a Successful Email Delivery

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

### Query All Attempts for an Event

```bash
curl http://localhost:8003/api/v1/delivery-attempts/events/evt_001
```

### Query Failed Attempts for Retry

```bash
curl "http://localhost:8003/api/v1/delivery-attempts/failed?since=2026-06-17T10:00:00Z&limit=100"
```

### Query User's Recent Notifications

```bash
curl http://localhost:8003/api/v1/delivery-attempts/users/usr_123
```
