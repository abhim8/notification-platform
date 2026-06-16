package notification.adapter.channels.email;

import lombok.extern.slf4j.Slf4j;
import notification.domain.channel.Channel;
import notification.domain.channel.ChannelDispatcher;
import notification.domain.channel.DispatchResult;
import notification.domain.event.NotificationEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Email channel adapter using SendGrid.
 *
 * Currently mocked - actual SendGrid API calls are disabled.
 * All sends are logged as mock operations for testing/demonstration.
 */
@Component
@Slf4j
public class SendGridAdapter implements ChannelDispatcher {

    @Value("${sendgrid.from-email:noreply@notification-platform.com}")
    private String fromEmail;

    @Value("${sendgrid.api-key:mock-key}")
    private String apiKey;

    @Override
    public DispatchResult dispatch(NotificationEvent event, String recipient, String content) {
        try {
            // Extract email subject from payload if available, else use event type
            String subject = event.payload().getOrDefault("subject",
                    "Notification - " + event.eventType()).toString();

            // Mock the SendGrid API call
            log.info("[MOCK][EMAIL] Would send to={} from={} subject={} eventId={} content_length={}",
                    recipient,
                    fromEmail,
                    subject,
                    event.eventId(),
                    content.length());

            // Return success with mock message ID
            String mockMessageId = "sendgrid_mock_" + event.eventId() + "_" + System.nanoTime();
            return DispatchResult.success(mockMessageId);

        } catch (Exception e) {
            log.error("[ERROR][EMAIL] Failed to send email: recipient={}, eventId={}",
                    recipient, event.eventId(), e);
            return DispatchResult.failure("Email dispatch failed: " + e.getMessage());
        }
    }

    @Override
    public Channel getChannel() {
        return Channel.EMAIL;
    }
}

