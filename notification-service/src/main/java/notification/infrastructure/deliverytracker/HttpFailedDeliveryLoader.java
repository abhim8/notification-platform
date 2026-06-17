package notification.infrastructure.deliverytracker;

import lombok.extern.slf4j.Slf4j;
import notification.application.usecase.RetryUseCase;
import notification.domain.channel.Channel;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
public class HttpFailedDeliveryLoader implements RetryUseCase.FailedDeliveryLoader {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public HttpFailedDeliveryLoader(RestTemplate restTemplate, String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public List<RetryUseCase.FailedDelivery> loadFailedDeliveries() {
        try {
            log.debug("[TRACKER] Loading failed deliveries via HTTP");
            String url = baseUrl + "/api/v1/delivery-attempts/failed?since={since}&limit={limit}";
            LocalDateTime since = LocalDateTime.now().minusDays(1);

            List<FailedDeliveryDto> dtos = restTemplate.exchange(
                    url, HttpMethod.GET, null,
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
}
