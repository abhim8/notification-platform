package notification.infrastructure.deliverytracker;

import lombok.extern.slf4j.Slf4j;
import notification.application.usecase.DeliveryAttemptCommand;
import notification.application.usecase.SendNotificationUseCase;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class HttpDeliveryAttemptRecorder implements SendNotificationUseCase.DeliveryAttemptRecorder {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public HttpDeliveryAttemptRecorder(RestTemplate restTemplate, String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public void recordAttempt(DeliveryAttemptCommand attempt) {
        try {
            log.debug("[TRACKER] Recording delivery attempt via HTTP: eventId={}, channel={}, status={}",
                    attempt.eventId(), attempt.channel(), attempt.status());
            String url = baseUrl + "/api/v1/delivery-attempts";
            restTemplate.postForEntity(url, attempt, Void.class);
        } catch (Exception e) {
            log.error("[TRACKER] Failed to record delivery attempt via HTTP: eventId={}, channel={}",
                    attempt.eventId(), attempt.channel(), e);
        }
    }
}
