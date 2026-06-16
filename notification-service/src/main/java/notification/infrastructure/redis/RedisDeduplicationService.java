package notification.infrastructure.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import notification.application.service.DeduplicationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis-backed implementation of DeduplicationService.
 *
 * Uses Redis with 24-hour TTL to prevent duplicate event processing.
 * Event IDs are keys, and the presence of a key indicates the event has been processed.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RedisDeduplicationService implements DeduplicationService {

    private static final String DEDUP_KEY_PREFIX = "dedup:event:";

    @Value("${idempotency.ttl-hours:24}")
    private long ttlHours;

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public boolean isDuplicate(String eventId) {
        try {
            String key = DEDUP_KEY_PREFIX + eventId;
            Boolean exists = redisTemplate.hasKey(key);

            if (Boolean.TRUE.equals(exists)) {
                log.debug("[DEDUP] Event found in cache: eventId={}", eventId);
                return true;
            }

            return false;
        } catch (Exception e) {
            log.error("[ERROR] Failed to check deduplication in Redis: eventId={}", eventId, e);
            // Fail open - if Redis is down, allow the event to be processed
            return false;
        }
    }

    @Override
    public void markProcessed(String eventId) {
        try {
            String key = DEDUP_KEY_PREFIX + eventId;
            redisTemplate.opsForValue().set(key, "processed", ttlHours, TimeUnit.HOURS);
            log.debug("[DEDUP] Event marked as processed: eventId={}, ttl_hours={}", eventId, ttlHours);
        } catch (Exception e) {
            log.error("[ERROR] Failed to mark event as processed in Redis: eventId={}", eventId, e);
            // Fail open - if Redis is down, continue processing
        }
    }

    @Override
    public void remove(String eventId) {
        try {
            String key = DEDUP_KEY_PREFIX + eventId;
            Boolean deleted = redisTemplate.delete(key);
            log.debug("[DEDUP] Event removed from cache: eventId={}, deleted={}", eventId, deleted);
        } catch (Exception e) {
            log.error("[ERROR] Failed to remove event from Redis: eventId={}", eventId, e);
        }
    }
}

