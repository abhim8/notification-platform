package notification.adapter.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import notification.application.usecase.SendNotificationResult;
import notification.application.usecase.SendNotificationUseCase;
import notification.domain.event.NotificationEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer that listens to notification topics and dispatches events.
 *
 * Listens to:
 * - notification.transactional (high priority, 7-day retention)
 * - notification.marketing (lower priority, 3-day retention)
 * - notification.alerts (urgent, 1-day retention)
 *
 * Partition key: userId (ensures ordering per user, allows scale-out)
 * Consumer group: notif-svc (single group for all topics)
 */
@Component
@Slf4j
public class NotificationKafkaConsumer {


    private final SendNotificationUseCase sendNotificationUseCase;
    private final NotificationKafkaProducer kafkaProducer;
    private final ObjectMapper objectMapper;

    public NotificationKafkaConsumer(
            SendNotificationUseCase sendNotificationUseCase,
            NotificationKafkaProducer kafkaProducer,
            ObjectMapper objectMapper) {
        this.sendNotificationUseCase = sendNotificationUseCase;
        this.kafkaProducer = kafkaProducer;
        this.objectMapper = objectMapper;
    }

    /**
     * Listen to transactional notifications (ORDER_PLACED, PAYMENT_FAILED, etc.)
     * High throughput, 6 partitions for parallel processing
     */
    @KafkaListener(
            topics = "notification.transactional",
            groupId = "notif-svc-trans",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onTransactionalNotification(
            @Payload String payload,
            @Header(name = "kafka_receivedPartitionId", required = false) int partition,
            @Header(name = "kafka_offset", required = false) long offset,
            @Header(name = "kafka_receivedTopic", required = false) String topic,
            Acknowledgment ack) {

        log.debug("[CONSUME] Transactional event received: topic={}, partition={}, offset={}",
                topic, partition, offset);
        processNotificationEvent(payload, "transactional", ack);
    }

    /**
     * Listen to marketing notifications (PROMOTION_ALERT, etc.)
     * Lower priority, 3 partitions
     */
    @KafkaListener(
            topics = "notification.marketing",
            groupId = "notif-svc-mktg",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onMarketingNotification(
            @Payload String payload,
            @Header(name = "kafka_receivedPartitionId", required = false) int partition,
            @Header(name = "kafka_offset", required = false) long offset,
            Acknowledgment ack) {

        log.debug("[CONSUME] Marketing event received: partition={}, offset={}", partition, offset);
        processNotificationEvent(payload, "marketing", ack);
    }

    /**
     * Listen to alert notifications (FRAUD_ALERT, SYSTEM_ALERT, etc.)
     * Urgent priority, 6 partitions for high throughput
     */
    @KafkaListener(
            topics = "notification.alerts",
            groupId = "notif-svc-alerts",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onAlertNotification(
            @Payload String payload,
            @Header(name = "kafka_receivedPartitionId", required = false) int partition,
            @Header(name = "kafka_offset", required = false) long offset,
            Acknowledgment ack) {

        log.debug("[CONSUME] Alert event received: partition={}, offset={}", partition, offset);
        processNotificationEvent(payload, "alerts", ack);
    }

    /**
     * Process a notification event consumed from Kafka
     */
    private void processNotificationEvent(String payload, String topic, Acknowledgment ack) {
        try {
            // Deserialize JSON to domain object
            NotificationEvent event = objectMapper.readValue(payload, NotificationEvent.class);

            log.info("[PROCESS] Processing event: eventId={}, eventType={}, userId={}, topic={}",
                    event.eventId(), event.eventType(), event.userId(), topic);

            // Execute the send notification use case
            SendNotificationResult result = sendNotificationUseCase.execute(event);

            if (result.success()) {
                log.info("[SUCCESS] Notification sent: eventId={}, status={}",
                        result.eventId(), result.status());
            } else {
                log.warn("[FAILURE] Notification failed: eventId={}, status={}, message={}",
                        result.eventId(), result.status(), result.message());

                // Failed notifications will be retried by the RetryUseCase scheduled task
                // No need to republish to retry topic here
            }

            // Ack only after a definitive outcome from the use case.
            // Unexpected exceptions (deserialization errors, network failures, etc.)
            // are deliberately left unacknowledged so the offset does not advance,
            // allowing redelivery or future DLQ routing.
            ack.acknowledge();

        } catch (Exception e) {
            log.error("[ERROR] Failed to process Kafka event: payload={}, errorType={}", payload, e.getClass().getSimpleName(), e);
            // Offset is intentionally NOT acknowledged so the message will be
            // redelivered on rebalance/restart. In production, a retry threshold
            // should route the message to a DLQ topic after N failures to avoid
            // blocking the partition.
        }
    }
}

