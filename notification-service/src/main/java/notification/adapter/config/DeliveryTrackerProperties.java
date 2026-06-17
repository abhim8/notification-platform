package notification.adapter.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "delivery-tracker")
public class DeliveryTrackerProperties {
    private boolean enabled;
    private String baseUrl;
    private Endpoints endpoints = new Endpoints();

    @Setter
    @Getter
    public static class Endpoints {
        private String recordAttempt;
        private String failedAttempts;
    }
}
