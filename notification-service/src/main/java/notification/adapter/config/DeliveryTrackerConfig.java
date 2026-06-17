package notification.adapter.config;

import notification.application.service.DeliveryAttemptRecorder;
import notification.application.service.FailedDeliveryLoader;
import notification.infrastructure.deliverytracker.HttpDeliveryAttemptRecorder;
import notification.infrastructure.deliverytracker.HttpFailedDeliveryLoader;
import notification.infrastructure.deliverytracker.InMemoryDeliveryStore;
import notification.infrastructure.deliverytracker.MockDeliveryAttemptRecorder;
import notification.infrastructure.deliverytracker.MockFailedDeliveryLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class DeliveryTrackerConfig {

    @Bean
    @ConditionalOnProperty(value = "delivery-tracker.enabled", havingValue = "true")
    public DeliveryAttemptRecorder httpDeliveryAttemptRecorder(
            RestTemplate restTemplate,
            @Value("${delivery-tracker.base-url}") String baseUrl) {
        return new HttpDeliveryAttemptRecorder(restTemplate, baseUrl);
    }

    @Bean
    @ConditionalOnProperty(value = "delivery-tracker.enabled", havingValue = "true")
    public FailedDeliveryLoader httpFailedDeliveryLoader(
            RestTemplate restTemplate,
            @Value("${delivery-tracker.base-url}") String baseUrl) {
        return new HttpFailedDeliveryLoader(restTemplate, baseUrl);
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
