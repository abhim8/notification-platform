package notification.adapter.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import notification.infrastructure.template.MockTemplateResolver;
import notification.application.service.TemplateResolver;

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
    @ConditionalOnMissingBean(TemplateResolver.class)
    public TemplateResolver templateResolver() {
        return new MockTemplateResolver();
    }
}

