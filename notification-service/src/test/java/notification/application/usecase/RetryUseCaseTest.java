package notification.application.usecase;

import com.notification.common.domain.Channel;
import com.notification.common.domain.EventType;
import notification.application.service.DeliveryAttemptRecorder;
import notification.application.service.FailedDeliveryLoader;
import notification.domain.model.RetryPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RetryUseCaseTest {

    @Mock
    FailedDeliveryLoader failedDeliveryLoader;
    @Mock
    SendNotificationUseCase sendNotificationUseCase;
    @Mock
    DeliveryAttemptRecorder attemptRecorder;

    private RetryUseCase retryUseCase;

    @BeforeEach
    void setUp() {
        retryUseCase = new RetryUseCase(
                RetryPolicy.defaultPolicy(),
                sendNotificationUseCase,
                failedDeliveryLoader,
                attemptRecorder
        );
    }

    @Test
    void shouldSkipRetriesNotYetDue() {
        when(failedDeliveryLoader.loadFailedDeliveries()).thenReturn(List.of(
                new RetryUseCase.FailedDelivery(
                        "evt-1", "u1", EventType.ORDER_PLACED,
                        Channel.EMAIL, 1, LocalDateTime.now().minusSeconds(1))
        ));

        RetryUseCase.RetryResult result = retryUseCase.execute();

        assertEquals(0, result.retriedCount());
        assertEquals(0, result.dlqCount());
    }

    @Test
    void shouldMoveToDlqAfterMaxRetries() {
        when(failedDeliveryLoader.loadFailedDeliveries()).thenReturn(List.of(
                new RetryUseCase.FailedDelivery(
                        "evt-1", "u1", EventType.ORDER_PLACED,
                        Channel.EMAIL, 3, LocalDateTime.now().minusHours(1))
        ));

        RetryUseCase.RetryResult result = retryUseCase.execute();

        assertEquals(0, result.retriedCount());
        assertEquals(1, result.dlqCount());
        verify(attemptRecorder).recordAttempt(argThat(cmd -> cmd.status() == com.notification.common.domain.DeliveryStatus.DLQ));
    }

    @Test
    void shouldRetryDueDelivery() {
        when(failedDeliveryLoader.loadFailedDeliveries()).thenReturn(List.of(
                new RetryUseCase.FailedDelivery(
                        "evt-1", "u1", EventType.ORDER_PLACED,
                        Channel.EMAIL, 1, LocalDateTime.now().minusMinutes(5))
        ));

        RetryUseCase.RetryResult result = retryUseCase.execute();

        assertEquals(1, result.retriedCount());
        assertEquals(0, result.dlqCount());
    }
}
