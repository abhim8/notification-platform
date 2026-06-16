package notification.adapter.channels.push;

import lombok.extern.slf4j.Slf4j;
import notification.domain.channel.Channel;
import notification.domain.channel.ChannelDispatcher;
import notification.domain.channel.DispatchResult;
import notification.domain.event.NotificationEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Push notification channel adapter using Firebase Cloud Messaging (FCM).
 *
 * Currently mocked - actual Firebase API calls are disabled.
 * All sends are logged as mock operations for testing/demonstration.
 */
@Component
@Slf4j
public class FcmAdapter implements ChannelDispatcher {

    @Value("${firebase.enabled:false}")
    private boolean firebaseEnabled;

    @Value("${firebase.project-id:mock-project}")
    private String projectId;

    @Override
    public DispatchResult dispatch(NotificationEvent event, String recipient, String content) {
        try {
            // Validate device token format (basic check)
            if (recipient == null || recipient.isBlank()) {
                return DispatchResult.failure("Push recipient (device token) is required");
            }

            // Extract notification title and body from payload
            String title = event.payload().getOrDefault("title",
                    "Notification - " + event.eventType()).toString();
            String body = truncateContent(content, 80);

            // Mock the Firebase FCM API call
            log.info("[MOCK][PUSH] Would send to device_token={} title={} body={} eventId={} firebase_enabled={}",
                    maskDeviceToken(recipient),
                    title,
                    body,
                    event.eventId(),
                    firebaseEnabled);

            // Return success with mock message ID
            String mockMessageId = "fcm_mock_" + event.eventId() + "_" + System.nanoTime();
            return DispatchResult.success(mockMessageId);

        } catch (Exception e) {
            log.error("[ERROR][PUSH] Failed to send push notification: recipient={}, eventId={}",
                    recipient, event.eventId(), e);
            return DispatchResult.failure("Push notification dispatch failed: " + e.getMessage());
        }
    }

    @Override
    public Channel getChannel() {
        return Channel.PUSH;
    }

    /**
     * Truncate content to a maximum length for logging
     */
    private String truncateContent(String content, int maxLength) {
        if (content == null) return "";
        if (content.length() <= maxLength) return content;
        return content.substring(0, maxLength) + "...";
    }

    /**
     * Mask device token for security in logs (show only first and last 8 chars)
     */
    private String maskDeviceToken(String token) {
        if (token == null || token.length() < 16) return "***";
        return token.substring(0, 8) + "..." + token.substring(token.length() - 8);
    }
}

