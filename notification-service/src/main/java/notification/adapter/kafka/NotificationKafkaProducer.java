package notification.adapter.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import notification.domain.event.NotificationEvent;

/**
 * Kafka producer for publishing notifications to retry and DLQ topics.
 *
 * Topics:
 * - notification.retry: Events that failed initial delivery and need retry
 * - notification.dlq: Events that exhausted all retries and are permanently failed
 *
 * Partition key: userId (ensures ordering per user)
 */
@Component
public class NotificationKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(NotificationKafkaProducer.class);

    private static final String RETRY_TOPIC = "notification.retry";
    private static final String DLQ_TOPIC = "notification.dlq";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public NotificationKafkaProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Publish an event to the retry topic.
     * Called when an event fails delivery and should be retried.
     *
     * @param event the notification event to retry
     */
    public void publishToRetry(NotificationEvent event) {
        publishToTopic(RETRY_TOPIC, event);
        log.info("[RETRY] Event published to retry topic: eventId={}, userId={}",
                event.eventId(), event.userId());
    }

    /**
     * Publish an event to the DLQ (Dead Letter Queue) topic.
     * Called when an event has exhausted all retry attempts.
     *
     * @param event the notification event that permanently failed
     */
    public void publishToDLQ(NotificationEvent event) {
        publishToTopic(DLQ_TOPIC, event);
        log.warn("[DLQ] Event published to DLQ: eventId={}, userId={}",
                event.eventId(), event.userId());
    }

    /**
     * Internal method to publish to a topic
     */
    private void publishToTopic(String topic, NotificationEvent event) {
        try {
            // Serialize event to JSON
            String payload = objectMapper.writeValueAsString(event);

            // Create message with partition key (userId) to ensure ordering
            Message<String> message = MessageBuilder
                    .withPayload(payload)
                    .setHeader(KafkaHeaders.TOPIC, topic)
                    .setHeader("kafka_messageKey", event.userId())
                    .build();

            kafkaTemplate.send(message).whenComplete((result, ex) -> {
                if (ex == null) {
                    log.debug("[PUBLISHED] Event published to topic={}: eventId={}, partition={}, offset={}",
                            topic, event.eventId(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("[ERROR] Failed to publish event to topic={}: eventId={}",
                            topic, event.eventId(), ex);
                }
            });

        } catch (Exception e) {
            log.error("[ERROR] Failed to serialize and publish event: eventId={}, topic={}",
                    event.eventId(), topic, e);
            throw new RuntimeException("Failed to publish event to Kafka", e);
        }
    }
}

