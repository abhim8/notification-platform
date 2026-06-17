package notification.adapter.config;

import notification.application.service.TemplateResolver;
import notification.infrastructure.template.MockTemplateResolver;
import notification.infrastructure.template.RestTemplateResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
     * Provide RestTemplateResolver when template-service integration is enabled.
     * When this bean is created, {@link #mockTemplateResolver()} is skipped
     * due to {@code @ConditionalOnMissingBean}.
     */
    @Bean
    @ConditionalOnProperty(value = "template-service.enabled", havingValue = "true")
    public TemplateResolver restTemplateResolver(
            RestTemplate restTemplate,
            @Value("${template-service.base-url}") String baseUrl) {
        return new RestTemplateResolver(restTemplate, baseUrl);
    }
}

