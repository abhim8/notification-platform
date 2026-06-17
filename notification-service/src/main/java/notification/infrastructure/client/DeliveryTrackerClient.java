package notification.infrastructure.client;

import lombok.extern.slf4j.Slf4j;
import notification.application.service.DeliveryAttemptRecorder;
import notification.application.service.FailedDeliveryLoader;
import notification.application.usecase.DeliveryAttemptCommand;
import notification.application.usecase.RetryUseCase;
import notification.domain.channel.Channel;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
public class DeliveryTrackerClient implements DeliveryAttemptRecorder, FailedDeliveryLoader {

    private final RestTemplate restTemplate;
    private final String recordAttemptUrl;
    private final String failedAttemptsUrl;

    public DeliveryTrackerClient(RestTemplate restTemplate, String recordAttemptUrl, String failedAttemptsUrl) {
        this.restTemplate = restTemplate;
        this.recordAttemptUrl = recordAttemptUrl;
        this.failedAttemptsUrl = failedAttemptsUrl;
    }

    @Override
    public void recordAttempt(DeliveryAttemptCommand attempt) {
        try {
            log.debug("[TRACKER] Recording delivery attempt via HTTP: eventId={}, channel={}, status={}",
                    attempt.eventId(), attempt.channel(), attempt.status());
            restTemplate.postForEntity(recordAttemptUrl, attempt, Void.class);
        } catch (Exception e) {
            log.error("[TRACKER] Failed to record delivery attempt via HTTP: eventId={}, channel={}",
                    attempt.eventId(), attempt.channel(), e);
        }
    }

    @Override
    public List<RetryUseCase.FailedDelivery> loadFailedDeliveries() {
        try {
            log.debug("[TRACKER] Loading failed deliveries via HTTP");
            LocalDateTime since = LocalDateTime.now().minusDays(1);

            List<FailedDeliveryDto> dtos = restTemplate.exchange(
                    failedAttemptsUrl, HttpMethod.GET, null,
                    new ParameterizedTypeReference<List<FailedDeliveryDto>>() {},
                    since.toString(), 100
            ).getBody();

            if (dtos == null || dtos.isEmpty()) {
                return List.of();
            }

            return dtos.stream()
                    .map(dto -> new RetryUseCase.FailedDelivery(
                            dto.eventId(),
                            dto.userId(),
                            dto.eventType(),
                            Channel.fromString(dto.channel()),
                            dto.attemptNumber(),
                            dto.updatedAt()
                    ))
                    .toList();

        } catch (Exception e) {
            log.error("[TRACKER] Failed to load failed deliveries via HTTP", e);
            return List.of();
        }
    }

    record FailedDeliveryDto(
            String eventId,
            String userId,
            String eventType,
            String channel,
            int attemptNumber,
            LocalDateTime updatedAt
    ) {}
}
