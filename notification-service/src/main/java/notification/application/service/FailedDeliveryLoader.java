package notification.application.service;

import notification.application.usecase.RetryUseCase;

import java.util.List;

public interface FailedDeliveryLoader {
    List<RetryUseCase.FailedDelivery> loadFailedDeliveries();
}
