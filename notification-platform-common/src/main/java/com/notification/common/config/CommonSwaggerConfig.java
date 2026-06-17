package com.notification.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

import java.util.List;

public abstract class CommonSwaggerConfig {

    protected OpenAPI buildOpenAPI(String title, String description, String serverUrl, String serverDescription) {
        return new OpenAPI()
                .info(new Info()
                        .title(title)
                        .version("1.0.0")
                        .description(description)
                        .contact(new Contact()
                                .name("Notification Platform Team")
                                .email("support@notification-platform.com")))
                .servers(List.of(
                        new Server()
                                .url(serverUrl)
                                .description(serverDescription)
                ));
    }
}
