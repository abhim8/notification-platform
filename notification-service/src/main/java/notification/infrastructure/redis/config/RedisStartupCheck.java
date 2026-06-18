package notification.infrastructure.redis.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisStartupCheck {

    private final LettuceConnectionFactory connectionFactory;

    @PostConstruct
    public void checkRedisConnection() {
        try (var connection = connectionFactory.getConnection()) {
            String pong = connection.ping();

            if (!"PONG".equalsIgnoreCase(pong)) {
                throw new IllegalStateException("Redis ping failed");
            }

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to connect to Redis during startup", e);
        }
    }
}
