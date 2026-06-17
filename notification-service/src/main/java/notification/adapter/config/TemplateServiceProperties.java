package notification.adapter.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Setter
@Getter
@ConfigurationProperties(prefix = "template-service")
public class TemplateServiceProperties {
    private boolean enabled;
    private String baseUrl;
    private Endpoints endpoints = new Endpoints();

    @Setter
    @Getter
    public static class Endpoints {
        private String getTemplate;
        private String renderTemplate;
    }
}
