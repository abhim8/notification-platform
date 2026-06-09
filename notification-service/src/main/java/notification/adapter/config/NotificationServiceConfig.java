package notification.adapter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import notification.application.service.DeduplicationService;
import notification.application.service.TemplateResolver;
import notification.application.usecase.RetryUseCase;
import notification.application.usecase.SendNotificationUseCase;
import notification.domain.channel.Channel;
import notification.domain.channel.ChannelDispatcher;
import notification.domain.model.RetryPolicy;
import notification.infrastructure.postgres.adapter.PostgresDeliveryAttemptRecorder;

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
     * SendNotificationUseCase bean
     */
    @Bean
    public SendNotificationUseCase sendNotificationUseCase(
            DeduplicationService deduplicationService,
            TemplateResolver templateResolver,
            Map<Channel, ChannelDispatcher> channelDispatchers,
            PostgresDeliveryAttemptRecorder deliveryAttemptRecorder) {

        return new SendNotificationUseCase(
                deduplicationService,
                templateResolver,
                channelDispatchers,
                deliveryAttemptRecorder
        );
    }

    /**
     * RetryUseCase bean
     */
    @Bean
    public RetryUseCase retryUseCase(
            RetryPolicy retryPolicy,
            SendNotificationUseCase sendNotificationUseCase,
            PostgresDeliveryAttemptRecorder deliveryAttemptRecorder) {

        // Stub implementation for FailedDeliveryLoader
        RetryUseCase.FailedDeliveryLoader failedDeliveryLoader = () -> java.util.List.of();

        return new RetryUseCase(
                retryPolicy,
                sendNotificationUseCase,
                failedDeliveryLoader,
                deliveryAttemptRecorder
        );
    }
}

