package notification.infrastructure.deliverytracker;

import notification.application.usecase.DeliveryAttemptCommand;
import com.notification.common.domain.DeliveryStatus;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class InMemoryDeliveryStore {

    private final List<StoredAttempt> attempts = new CopyOnWriteArrayList<>();

    public void add(DeliveryAttemptCommand attempt) {
        attempts.add(new StoredAttempt(attempt, LocalDateTime.now()));
    }

    public List<StoredAttempt> getFailedDeliveriesSince(LocalDateTime since, int limit) {
        return attempts.stream()
                .filter(a -> a.attempt().status() == DeliveryStatus.FAILED)
                .filter(a -> a.timestamp().isAfter(since))
                .sorted(Comparator.comparing(StoredAttempt::timestamp))
                .limit(limit)
                .toList();
    }

    record StoredAttempt(DeliveryAttemptCommand attempt, LocalDateTime timestamp) {}
}
