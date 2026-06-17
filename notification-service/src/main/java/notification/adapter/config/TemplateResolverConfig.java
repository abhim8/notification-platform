package notification.adapter.config;

import notification.application.service.TemplateResolver;
import notification.infrastructure.client.TemplateServiceClient;
import notification.infrastructure.template.MockTemplateResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Conditional bean configuration - provides MockTemplateResolver
 * if no real TemplateResolver is available.
 *
 * In production, this would be replaced by a client that calls
 * the actual template-service REST API.
 */
@Configuration
@EnableConfigurationProperties(TemplateServiceProperties.class)
public class TemplateResolverConfig {

    /**
     * Provide MockTemplateResolver if no TemplateResolver bean exists.
     * This is useful for development and testing.
     */
    @Bean
    @ConditionalOnProperty(value = "template-service.enabled", havingValue = "false")
    public TemplateResolver mockTemplateResolver() {
        return new MockTemplateResolver();
    }

    /**
     * Provide TemplateServiceClient when template-service integration is enabled.
     */
    @Bean
    @ConditionalOnProperty(value = "template-service.enabled", havingValue = "true")
    public TemplateResolver templateServiceClient(
            RestTemplate restTemplate,
            TemplateServiceProperties properties) {
        String baseUrl = properties.getBaseUrl();
        var endpoints = properties.getEndpoints();
        return new TemplateServiceClient(
                restTemplate,
                baseUrl + endpoints.getGetTemplate(),
                baseUrl + endpoints.getRenderTemplate());
    }
}

