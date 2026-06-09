package delivery.adapter.rest.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI configuration for the delivery-tracker.
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Delivery Tracker API")
                        .version("1.0.0")
                        .description("Delivery attempt tracking and history service")
                        .contact(new Contact()
                                .name("Notification Platform Team")
                                .email("support@notification-platform.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8082")
                                .description("Local development server"),
                        new Server()
                                .url("http://delivery-tracker:8082")
                                .description("Docker container")
                ));
    }
}

