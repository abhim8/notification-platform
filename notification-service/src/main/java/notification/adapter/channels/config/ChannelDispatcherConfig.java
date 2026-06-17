package notification.adapter.channels.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import notification.adapter.channels.email.SendGridAdapter;
import notification.adapter.channels.push.FcmAdapter;
import notification.adapter.channels.sms.TwilioAdapter;
import notification.adapter.channels.webhook.WebhookAdapter;
import com.notification.common.domain.Channel;
import notification.domain.channel.ChannelDispatcher;

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
     */
    @Bean
    public RestTemplate restTemplate() {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(10000);
        return new RestTemplate(factory);
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

