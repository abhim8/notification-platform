package notification.infrastructure.deliverytracker;

import lombok.extern.slf4j.Slf4j;
import notification.application.service.FailedDeliveryLoader;
import notification.application.usecase.RetryUseCase;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
public class MockFailedDeliveryLoader implements FailedDeliveryLoader {

    private final InMemoryDeliveryStore store;

    public MockFailedDeliveryLoader(InMemoryDeliveryStore store) {
        this.store = store;
    }

    @Override
    public List<RetryUseCase.FailedDelivery> loadFailedDeliveries() {
        log.debug("[TRACKER] Loading failed deliveries (mock)");
        LocalDateTime since = LocalDateTime.now().minusDays(1);

        return store.getFailedDeliveriesSince(since, 100).stream()
                .map(stored -> {
                    var a = stored.attempt();
                    return new RetryUseCase.FailedDelivery(
                            a.eventId(),
                            a.userId(),
                            a.eventType(),
                            a.channel(),
                            a.attemptNumber(),
                            stored.timestamp()
                    );
                })
                .toList();
    }
}
