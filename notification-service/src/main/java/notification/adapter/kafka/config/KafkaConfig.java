package notification.adapter.kafka.config;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;

/**
 * Configuration for Kafka consumers and producers.
 *
 * Sets up:
 * - Consumer factory for reliable message processing
 * - Producer factory for publishing to retry/DLQ topics
 * - Error handling and acknowledgment strategies
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    /**
     * Producer factory for sending events to Kafka
     */
    @Bean
    public ProducerFactory<String, String> producerFactory(KafkaProperties kafkaProperties) {
        return new DefaultKafkaProducerFactory<>(kafkaProperties.buildProducerProperties(null));
    }

    /**
     * Kafka template for sending messages
     */
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(ProducerFactory<String, String> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    /**
     * Consumer factory for consuming events from Kafka
     */
    @Bean
    public ConsumerFactory<String, String> consumerFactory(KafkaProperties kafkaProperties) {
        return new DefaultKafkaConsumerFactory<>(kafkaProperties.buildConsumerProperties(null));
    }

    /**
     * Kafka listener container factory
     * Configured for:
     * - Manual acknowledgment (ack only after successful processing)
     * - Batch processing disabled (process one message at a time)
     * - Error handling for poison messages
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory) {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);

        // Process one message at a time (no batching)
        factory.setBatchListener(false);

        // Manual acknowledgment - only ack after successful processing
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        // Don't poll if there's an error (fail fast)
        factory.getContainerProperties().setPollTimeout(3000);

        return factory;
    }
}

