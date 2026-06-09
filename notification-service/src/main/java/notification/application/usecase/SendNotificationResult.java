package notification.application.usecase;

import notification.domain.channel.DispatchResult;

import java.util.Map;

/**
 * Result of executing the SendNotificationUseCase.
 * Contains overall outcome and per-channel results.
 */
public record SendNotificationResult(
    String eventId,
    boolean success,
    String status,  // "success", "failed", "dropped"
    String message,
    Map<String, DispatchResult> channelResults
) {

    /**
     * Create a successful result
     */
    public static SendNotificationResult success(String eventId, Map<String, DispatchResult> channelResults) {
        return new SendNotificationResult(
                eventId,
                true,
                "success",
                "Notification sent successfully",
                channelResults
        );
    }

    /**
     * Create a failed result
     */
    public static SendNotificationResult failed(String eventId, String errorMessage) {
        return new SendNotificationResult(
                eventId,
                false,
                "failed",
                errorMessage,
                Map.of()
        );
    }

    /**
     * Create a dropped result (deduplicated)
     */
    public static SendNotificationResult dropped(String eventId, String reason) {
        return new SendNotificationResult(
                eventId,
                false,
                "dropped",
                reason,
                Map.of()
        );
    }
}

