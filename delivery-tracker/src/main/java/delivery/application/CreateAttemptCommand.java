package delivery.application;

public record CreateAttemptCommand(
    String eventId,
    String userId,
    String eventType,
    String channel,
    String status,
    int attemptNumber,
    String messageId,
    String errorMessage
) {}
