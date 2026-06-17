package notification.adapter.config;

import notification.application.service.DeduplicationService;
import notification.application.service.TemplateResolver;
import notification.application.usecase.RetryUseCase;
import notification.application.usecase.SendNotificationUseCase;
import notification.domain.channel.Channel;
import notification.domain.channel.ChannelDispatcher;
import notification.domain.model.RetryPolicy;
import notification.infrastructure.deliverytracker.HttpDeliveryAttemptRecorder;
import notification.infrastructure.deliverytracker.HttpFailedDeliveryLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * Configuration for wiring application layer components.
 *
 * Creates beans for all use cases and services.
 */
@Configuration
public class NotificationServiceConfig {

    /**
     * Default retry policy bean
     */
    @Bean
    public RetryPolicy retryPolicy() {
        return RetryPolicy.defaultPolicy();
    }

    /**
     * HTTP-based delivery attempt recorder that delegates to delivery-tracker service
     */
    @Bean
    @ConditionalOnProperty(value = "delivery-tracker.enabled", havingValue = "true")
    public HttpDeliveryAttemptRecorder httpDeliveryAttemptRecorder(
            RestTemplate restTemplate,
            @Value("${delivery-tracker.base-url}") String baseUrl) {
        return new HttpDeliveryAttemptRecorder(restTemplate, baseUrl);
    }

    /**
     * SendNotificationUseCase bean
     */
    @Bean
    public SendNotificationUseCase sendNotificationUseCase(
            DeduplicationService deduplicationService,
            TemplateResolver templateResolver,
            Map<Channel, ChannelDispatcher> channelDispatchers,
            HttpDeliveryAttemptRecorder deliveryAttemptRecorder) {

        return new SendNotificationUseCase(
                deduplicationService,
                templateResolver,
                channelDispatchers,
                deliveryAttemptRecorder
        );
    }

    /**
     * HTTP-based failed delivery loader that queries delivery-tracker service
     */
    @Bean
    @ConditionalOnProperty(value = "delivery-tracker.enabled", havingValue = "true")
    public HttpFailedDeliveryLoader httpFailedDeliveryLoader(
            RestTemplate restTemplate,
            @Value("${delivery-tracker.base-url}") String baseUrl) {
        return new HttpFailedDeliveryLoader(restTemplate, baseUrl);
    }

    /**
     * RetryUseCase bean
     */
    @Bean
    public RetryUseCase retryUseCase(
            RetryPolicy retryPolicy,
            SendNotificationUseCase sendNotificationUseCase,
            HttpDeliveryAttemptRecorder deliveryAttemptRecorder,
            HttpFailedDeliveryLoader failedDeliveryLoader) {

        return new RetryUseCase(
                retryPolicy,
                sendNotificationUseCase,
                failedDeliveryLoader,
                deliveryAttemptRecorder
        );
    }
}

