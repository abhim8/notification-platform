package delivery.adapter.rest.dto;

public record CreateDeliveryAttemptRequest(
    String eventId,
    String userId,
    String eventType,
    String channel,
    String status,
    int attemptNumber,
    String messageId,
    String errorMessage
) {}
