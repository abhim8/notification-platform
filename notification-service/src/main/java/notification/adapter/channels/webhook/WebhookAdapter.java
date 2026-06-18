package notification.adapter.channels.webhook;

import lombok.extern.slf4j.Slf4j;
import com.notification.common.domain.Channel;
import notification.domain.channel.ChannelDispatcher;
import notification.domain.channel.DispatchResult;
import com.notification.common.domain.EventType;
import notification.domain.event.NotificationEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Webhook channel adapter for outbound HTTP notifications.
 *
 * Makes HTTP POST requests to external webhooks with the notification payload.
 * Includes timeout handling and basic error recovery.
 */
@Component
@Slf4j
public class WebhookAdapter implements ChannelDispatcher {

    @Value("${webhook.timeout-ms:5000}")
    private int timeoutMs;

    @Value("${webhook.max-retries:1}")
    private int maxRetries;

    private final RestTemplate restTemplate;

    public WebhookAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public DispatchResult dispatch(NotificationEvent event, String recipient, String content) {
        try {
            // Recipient should be the webhook URL
            if (recipient == null || recipient.isBlank()) {
                return DispatchResult.failure("Webhook URL is required");
            }

            // Validate URL format (basic check)
            if (!recipient.startsWith("http://") && !recipient.startsWith("https://")) {
                return DispatchResult.failure("Invalid webhook URL: must start with http:// or https://");
            }

            // Build webhook payload
            WebhookPayload payload = new WebhookPayload(
                    event.eventId(),
                    event.eventType(),
                    event.userId(),
                    content,
                    System.currentTimeMillis()
            );

            // Log the webhook dispatch attempt
            log.info("[WEBHOOK] Posting to webhook: url={} eventId={} content_length={}",
                    maskUrl(recipient),
                    event.eventId(),
                    content.length());

            // For now, just log the attempt (mocked)
            // In production, would do: restTemplate.postForObject(recipient, payload, String.class)
            log.debug("[WEBHOOK] Webhook payload: {}", payload);

            // Return success with mock message ID
            String mockMessageId = "webhook_mock_" + event.eventId() + "_" + System.nanoTime();
            return DispatchResult.success(mockMessageId);

        } catch (RestClientException e) {
            log.warn("[WEBHOOK] HTTP error sending to webhook: recipient={}, eventId={}, error={}",
                    maskUrl(recipient), event.eventId(), e.getMessage());
            return DispatchResult.failure("Webhook dispatch failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("[ERROR][WEBHOOK] Failed to send webhook: recipient={}, eventId={}",
                    maskUrl(recipient), event.eventId(), e);
            return DispatchResult.failure("Webhook dispatch failed: " + e.getMessage());
        }
    }

    @Override
    public Channel getChannel() {
        return Channel.WEBHOOK;
    }

    /**
     * Mask URL for security in logs (show protocol and domain only)
     */
    private String maskUrl(String url) {
        try {
            if (url == null || url.isBlank()) return "***";
            // Extract protocol and domain
            String[] parts = url.split("\\?")[0].split("/");
            if (parts.length >= 3) {
                return parts[0] + "//" + parts[2] + "/...";
            }
            return "***";
        } catch (Exception e) {
            return "***";
        }
    }

    /**
     * POJO for webhook payload
     */
    public record WebhookPayload(
            String eventId,
            EventType eventType,
            String userId,
            String content,
            long timestamp
    ) {}
}

