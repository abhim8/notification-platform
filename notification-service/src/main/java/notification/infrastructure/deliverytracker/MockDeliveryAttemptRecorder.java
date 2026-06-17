package notification.infrastructure.deliverytracker;

import lombok.extern.slf4j.Slf4j;
import notification.application.service.DeliveryAttemptRecorder;
import notification.application.usecase.DeliveryAttemptCommand;

@Slf4j
public class MockDeliveryAttemptRecorder implements DeliveryAttemptRecorder {

    private final InMemoryDeliveryStore store;

    public MockDeliveryAttemptRecorder(InMemoryDeliveryStore store) {
        this.store = store;
    }

    @Override
    public void recordAttempt(DeliveryAttemptCommand attempt) {
        log.debug("[TRACKER] Recording delivery attempt (mock): eventId={}, channel={}, status={}",
                attempt.eventId(), attempt.channel(), attempt.status());
        store.add(attempt);
    }
}
