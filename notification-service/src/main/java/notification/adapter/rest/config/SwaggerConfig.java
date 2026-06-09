package notification.adapter.rest.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI configuration for the notification-service.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Notification Service API")
                        .version("1.0.0")
                        .description("Event-driven notification service for multi-channel delivery")
                        .contact(new Contact()
                                .name("Notification Platform Team")
                                .email("support@notification-platform.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8001")
                                .description("Local development server"),
                        new Server()
                                .url("http://notification-service:8001")
                                .description("Docker container")
                ));
    }
}

