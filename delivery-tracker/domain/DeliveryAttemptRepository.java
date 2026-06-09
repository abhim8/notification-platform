package delivery.domain;

import java.util.List;
import java.util.Optional;

public interface DeliveryAttemptRepository {

    void save(DeliveryAttempt attempt);

    Optional<DeliveryAttempt> findById(String attemptId);

    List<DeliveryAttempt> findByDeliveryId(String deliveryId);

    List<DeliveryAttempt> findByDeliveryIdAndChannel(String deliveryId, String channel);
}

