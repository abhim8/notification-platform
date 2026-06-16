package notification.adapter.channels.sms;

import lombok.extern.slf4j.Slf4j;
import notification.domain.channel.Channel;
import notification.domain.channel.ChannelDispatcher;
import notification.domain.channel.DispatchResult;
import notification.domain.event.NotificationEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * SMS channel adapter using Twilio.
 *
 * Currently mocked - actual Twilio API calls are disabled.
 * All sends are logged as mock operations for testing/demonstration.
 */
@Component
@Slf4j
public class TwilioAdapter implements ChannelDispatcher {

    @Value("${twilio.from-number:+1234567890}")
    private String fromNumber;

    @Value("${twilio.account-sid:mock-sid}")
    private String accountSid;

    @Override
    public DispatchResult dispatch(NotificationEvent event, String recipient, String content) {
        try {
            // Validate phone number format (basic check)
            if (recipient == null || recipient.isBlank()) {
                return DispatchResult.failure("SMS recipient (phone number) is required");
            }

            // Mock the Twilio API call
            log.info("[MOCK][SMS] Would send to={} from={} eventId={} content_length={} content_preview={}",
                    recipient,
                    fromNumber,
                    event.eventId(),
                    content.length(),
                    truncateContent(content, 50));

            // Return success with mock message ID
            String mockMessageId = "twilio_mock_" + event.eventId() + "_" + System.nanoTime();
            return DispatchResult.success(mockMessageId);

        } catch (Exception e) {
            log.error("[ERROR][SMS] Failed to send SMS: recipient={}, eventId={}",
                    recipient, event.eventId(), e);
            return DispatchResult.failure("SMS dispatch failed: " + e.getMessage());
        }
    }

    @Override
    public Channel getChannel() {
        return Channel.SMS;
    }

    /**
     * Truncate content to a maximum length for logging
     */
    private String truncateContent(String content, int maxLength) {
        if (content == null) return "";
        if (content.length() <= maxLength) return content;
        return content.substring(0, maxLength) + "...";
    }
}

