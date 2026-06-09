package delivery.domain;

public class DeliveryAttempt {
    private final String attemptId;
    private final String deliveryId;
    private final String channel;
    private final int attemptNumber;
    private final String status;
    private final String responseCode;
    private final String responseMessage;
    private final long timestamp;

    public DeliveryAttempt(
            String attemptId,
            String deliveryId,
            String channel,
            int attemptNumber,
            String status) {
        this.attemptId = attemptId;
        this.deliveryId = deliveryId;
        this.channel = channel;
        this.attemptNumber = attemptNumber;
        this.status = status;
        this.responseCode = null;
        this.responseMessage = null;
        this.timestamp = System.currentTimeMillis();
    }

    public DeliveryAttempt(
            String attemptId,
            String deliveryId,
            String channel,
            int attemptNumber,
            String status,
            String responseCode,
            String responseMessage) {
        this.attemptId = attemptId;
        this.deliveryId = deliveryId;
        this.channel = channel;
        this.attemptNumber = attemptNumber;
        this.status = status;
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.timestamp = System.currentTimeMillis();
    }

    public String getAttemptId() {
        return attemptId;
    }

    public String getDeliveryId() {
        return deliveryId;
    }

    public String getChannel() {
        return channel;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public String getStatus() {
        return status;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public long getTimestamp() {
        return timestamp;
    }
}

