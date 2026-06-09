package template.adapter.rest.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI configuration for the template-service.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Template Service API")
                        .version("1.0.0")
                        .description("Template management service for notification rendering")
                        .contact(new Contact()
                                .name("Notification Platform Team")
                                .email("support@notification-platform.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Local development server"),
                        new Server()
                                .url("http://template-service:8081")
                                .description("Docker container")
                ));
    }
}

