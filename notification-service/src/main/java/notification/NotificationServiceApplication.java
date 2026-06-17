package notification;

import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Notification Service application.
 *
 * This Spring Boot application handles:
 * - Consuming notification events from Kafka
 * - Dispatching notifications through multiple channels (Email, SMS, Push, Webhook)
 * - Retry logic for failed deliveries
 * - Idempotency handling for duplicate requests
 * - Health checks and monitoring
 */
@SpringBootApplication
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT10M")
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }

}

