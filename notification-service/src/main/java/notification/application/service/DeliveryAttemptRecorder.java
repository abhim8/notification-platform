package notification.application.service;

import notification.application.usecase.DeliveryAttemptCommand;

public interface DeliveryAttemptRecorder {
    void recordAttempt(DeliveryAttemptCommand attempt);
}
