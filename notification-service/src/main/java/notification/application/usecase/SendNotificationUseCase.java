package notification.application.usecase;

import lombok.extern.slf4j.Slf4j;
import notification.application.service.DeduplicationService;
import notification.application.service.DeliveryAttemptRecorder;
import notification.application.service.TemplateResolver;
import com.notification.common.domain.Channel;
import notification.domain.channel.ChannelDispatcher;
import notification.domain.channel.DispatchResult;
import notification.domain.event.NotificationEvent;
import com.notification.common.domain.DeliveryStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Use case for sending notifications through multiple channels.
 *
 * Orchestrates:
 * 1. Deduplication check (against duplicate eventIds)
 * 2. Template resolution
 * 3. Multi-channel dispatch
 * 4. Attempt tracking
 *
 * This is an application-level orchestrator using domain ports.
 */
@Slf4j
public class SendNotificationUseCase {

    private final DeduplicationService deduplicationService;
    private final TemplateResolver templateResolver;
    private final Map<Channel, ChannelDispatcher> channelDispatchers;
    private final DeliveryAttemptRecorder attemptRecorder;

    public SendNotificationUseCase(
            DeduplicationService deduplicationService,
            TemplateResolver templateResolver,
            Map<Channel, ChannelDispatcher> channelDispatchers,
            DeliveryAttemptRecorder attemptRecorder) {
        this.deduplicationService = deduplicationService;
        this.templateResolver = templateResolver;
        this.channelDispatchers = new HashMap<>(channelDispatchers);
        this.attemptRecorder = attemptRecorder;
    }

    /**
     * Execute the send notification use case
     *
     * @param event the notification event to send
     * @return the result of the notification dispatch
     */
    public SendNotificationResult execute(NotificationEvent event) {
        // Validate event
        event.validate();

        // Check for duplicates
        if (deduplicationService.isDuplicate(event.eventId())) {
            log.info("[DEDUP] Event already processed: eventId={}", event.eventId());
            return SendNotificationResult.dropped(event.eventId(), "Duplicate event within 24h window");
        }

        // Mark as processed
        deduplicationService.markProcessed(event.eventId());

        // Resolve template
        String renderedContent;
        try {
            renderedContent = templateResolver.resolveTemplate(event.templateId(), event.payload());
        } catch (Exception e) {
            log.error("[ERROR] Failed to resolve template: templateId={}, eventId={}", event.templateId(), event.eventId(), e);
            return SendNotificationResult.failed(event.eventId(), "Template resolution failed: " + e.getMessage());
        }

        // Dispatch through all requested channels
        Map<String, DispatchResult> channelResults = new HashMap<>();
        boolean hasSuccess = false;
        boolean hasFailure = false;

        for (Channel channel : event.channels()) {
            ChannelDispatcher dispatcher = channelDispatchers.get(channel);

            if (dispatcher == null) {
                log.warn("[WARN] No dispatcher found for channel: {}", channel);
                channelResults.put(channel.getValue(), DispatchResult.failure("No dispatcher registered"));
                hasFailure = true;
                continue;
            }

            // Extract recipient from payload (format: channel_recipient, e.g., "email_user@example.com")
            Object recipientObj = event.payload().get(channel.recipientKey());
            String recipient = recipientObj != null ? recipientObj.toString() : null;

            if (recipient == null || recipient.isBlank()) {
                log.warn("[WARN] No recipient found for channel {}: eventId={}", channel, event.eventId());
                channelResults.put(channel.getValue(), DispatchResult.failure("No recipient provided"));
                hasFailure = true;
                continue;
            }

            // Dispatch through channel
            DispatchResult result = dispatcher.dispatch(event, recipient, renderedContent);
            channelResults.put(channel.getValue(), result);

            if (result.success()) {
                log.info("[SUCCESS] Event dispatched: eventId={}, channel={}, messageId={}",
                        event.eventId(), channel, result.messageId());
                hasSuccess = true;
            } else {
                log.warn("[FAILURE] Event dispatch failed: eventId={}, channel={}, error={}",
                        event.eventId(), channel, result.errorMessage());
                hasFailure = true;
            }

            // Record attempt
            attemptRecorder.recordAttempt(new DeliveryAttemptCommand(
                    event.eventId(),
                    event.userId(),
                    event.eventType(),
                    channel,
                    result.success() ? DeliveryStatus.DELIVERED : DeliveryStatus.FAILED,
                    1,
                    result.messageId(),
                    result.errorMessage()
            ));
        }

        if (!hasSuccess && hasFailure) {
            return SendNotificationResult.failed(event.eventId(), "All channels failed");
        }

        return SendNotificationResult.success(event.eventId(), channelResults);
    }

}
