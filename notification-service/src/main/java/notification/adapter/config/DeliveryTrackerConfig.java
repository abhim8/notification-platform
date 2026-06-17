package notification.adapter.config;

import notification.application.service.DeliveryAttemptRecorder;
import notification.application.service.FailedDeliveryLoader;
import notification.infrastructure.client.DeliveryTrackerClient;
import notification.infrastructure.deliverytracker.InMemoryDeliveryStore;
import notification.infrastructure.deliverytracker.MockDeliveryAttemptRecorder;
import notification.infrastructure.deliverytracker.MockFailedDeliveryLoader;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableConfigurationProperties(DeliveryTrackerProperties.class)
public class DeliveryTrackerConfig {

    @Bean
    @ConditionalOnProperty(value = "delivery-tracker.enabled", havingValue = "true")
    public DeliveryTrackerClient deliveryTrackerClient(
            RestTemplate restTemplate,
            DeliveryTrackerProperties properties) {
        String baseUrl = properties.getBaseUrl();
        var endpoints = properties.getEndpoints();
        return new DeliveryTrackerClient(
                restTemplate,
                baseUrl + endpoints.getRecordAttempt(),
                baseUrl + endpoints.getFailedAttempts());
    }

    @Bean
    @ConditionalOnProperty(value = "delivery-tracker.enabled", havingValue = "false")
    public InMemoryDeliveryStore inMemoryDeliveryStore() {
        return new InMemoryDeliveryStore();
    }

    @Bean
    @ConditionalOnProperty(value = "delivery-tracker.enabled", havingValue = "false")
    public DeliveryAttemptRecorder mockDeliveryAttemptRecorder(InMemoryDeliveryStore store) {
        return new MockDeliveryAttemptRecorder(store);
    }

    @Bean
    @ConditionalOnProperty(value = "delivery-tracker.enabled", havingValue = "false")
    public FailedDeliveryLoader mockFailedDeliveryLoader(InMemoryDeliveryStore store) {
        return new MockFailedDeliveryLoader(store);
    }
}
