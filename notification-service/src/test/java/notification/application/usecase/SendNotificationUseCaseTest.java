package notification.application.usecase;

import com.notification.common.domain.Channel;
import com.notification.common.domain.EventType;
import notification.application.service.DeduplicationService;
import notification.application.service.DeliveryAttemptRecorder;
import notification.application.service.TemplateResolver;
import notification.domain.channel.ChannelDispatcher;
import notification.domain.channel.DispatchResult;
import notification.domain.event.NotificationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SendNotificationUseCaseTest {

    @Mock
    DeduplicationService deduplicationService;
    @Mock
    TemplateResolver templateResolver;
    @Mock
    DeliveryAttemptRecorder attemptRecorder;
    @Mock
    ChannelDispatcher emailDispatcher;
    @Mock
    ChannelDispatcher smsDispatcher;

    private SendNotificationUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SendNotificationUseCase(
                deduplicationService,
                templateResolver,
                Map.of(Channel.EMAIL, emailDispatcher, Channel.SMS, smsDispatcher),
                attemptRecorder
        );
    }

    @Test
    void shouldDropDuplicateEvent() {
        when(deduplicationService.isDuplicate("dup-1")).thenReturn(true);

        NotificationEvent event = NotificationEvent.create(
                "dup-1", EventType.ORDER_PLACED, "user-1",
                List.of(Channel.EMAIL), "tpl-1", Map.of());

        SendNotificationResult result = useCase.execute(event);

        assertFalse(result.success());
        assertEquals(notification.domain.model.NotificationStatus.DROPPED, result.status());
        verifyNoInteractions(templateResolver);
    }

    @Test
    void shouldSendToAllChannels() {
        when(deduplicationService.isDuplicate(any())).thenReturn(false);
        when(templateResolver.resolveTemplate(any(), any())).thenReturn("Hello!");

        when(emailDispatcher.dispatch(any(), eq("a@b.com"), any())).thenReturn(DispatchResult.success("email-id"));
        when(smsDispatcher.dispatch(any(), eq("+123"), any())).thenReturn(DispatchResult.success("sms-id"));

        NotificationEvent event = NotificationEvent.create(
                "evt-1", EventType.ORDER_PLACED, "user-1",
                List.of(Channel.EMAIL, Channel.SMS), "tpl-1",
                Map.of("email_recipient", "a@b.com", "sms_recipient", "+123", "orderId", "123"));

        SendNotificationResult result = useCase.execute(event);

        assertTrue(result.success());
        verify(emailDispatcher).dispatch(any(), eq("a@b.com"), any());
        verify(smsDispatcher).dispatch(any(), eq("+123"), any());
        verify(attemptRecorder, times(2)).recordAttempt(any());
    }

    @Test
    void shouldFailWhenNoRecipient() {
        when(deduplicationService.isDuplicate(any())).thenReturn(false);
        when(templateResolver.resolveTemplate(any(), any())).thenReturn("Hello!");

        NotificationEvent event = NotificationEvent.create(
                "evt-2", EventType.ORDER_PLACED, "user-1",
                List.of(Channel.EMAIL), "tpl-1", Map.of());

        SendNotificationResult result = useCase.execute(event);

        assertFalse(result.success());
        verify(emailDispatcher, never()).dispatch(any(), any(), any());
    }

    @Test
    void shouldFailOnTemplateError() {
        when(deduplicationService.isDuplicate(any())).thenReturn(false);
        when(templateResolver.resolveTemplate(any(), any())).thenThrow(new RuntimeException("Template down"));

        NotificationEvent event = NotificationEvent.create(
                "evt-3", EventType.ORDER_PLACED, "user-1",
                List.of(Channel.EMAIL), "tpl-1", Map.of("email_recipient", "a@b.com"));

        SendNotificationResult result = useCase.execute(event);

        assertFalse(result.success());
        assertEquals(notification.domain.model.NotificationStatus.FAILED, result.status());
    }

    @Test
    void shouldFailWhenDispatcherMissing() {
        when(deduplicationService.isDuplicate(any())).thenReturn(false);
        when(templateResolver.resolveTemplate(any(), any())).thenReturn("Hello!");

        NotificationEvent event = NotificationEvent.create(
                "evt-4", EventType.ORDER_PLACED, "user-1",
                List.of(Channel.PUSH), "tpl-1", Map.of("push_recipient", "token"));

        SendNotificationResult result = useCase.execute(event);

        assertFalse(result.success());
    }
}
