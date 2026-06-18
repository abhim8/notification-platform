package notification.adapter.channels.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import notification.adapter.channels.email.SendGridAdapter;
import notification.adapter.channels.push.FcmAdapter;
import notification.adapter.channels.sms.TwilioAdapter;
import notification.adapter.channels.webhook.WebhookAdapter;
import com.notification.common.domain.Channel;
import notification.domain.channel.ChannelDispatcher;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for channel dispatchers.
 * Registers all channel adapters as a map for dependency injection.
 */
@Configuration
public class ChannelDispatcherConfig {

    /**
     * RestTemplate bean for all outbound HTTP calls.
     * Configured with sensible connect/read timeouts.
     * Defense-in-depth: removes any XML message converters to ensure
     * JSON is always the default serialization format for REST communication.
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        RestTemplate restTemplate = builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();

        restTemplate.getMessageConverters().removeIf(
                c -> c instanceof MappingJackson2XmlHttpMessageConverter
        );

        return restTemplate;
    }

    /**
     * Create a map of Channel -> ChannelDispatcher implementations
     * This allows the SendNotificationUseCase to look up dispatchers by channel type
     */
    @Bean
    public Map<Channel, ChannelDispatcher> channelDispatchers(
            SendGridAdapter sendGridAdapter,
            TwilioAdapter twilioAdapter,
            FcmAdapter fcmAdapter,
            WebhookAdapter webhookAdapter) {

        Map<Channel, ChannelDispatcher> dispatchers = new HashMap<>();
        dispatchers.put(Channel.EMAIL, sendGridAdapter);
        dispatchers.put(Channel.SMS, twilioAdapter);
        dispatchers.put(Channel.PUSH, fcmAdapter);
        dispatchers.put(Channel.WEBHOOK, webhookAdapter);

        return dispatchers;
    }
}

