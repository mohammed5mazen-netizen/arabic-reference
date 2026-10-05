package com.mrsoft.arabicreference.tools.infrastructure;

import com.mrsoft.arabicreference.shared.kernel.exception.RateLimitedException;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * A generous per-minute ceiling for anonymous tools. Redis failure does not block the visitor.
 */
@Component
public class ToolRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(ToolRateLimiter.class);

    private final StringRedisTemplate redis;
    private final int limit;

    public ToolRateLimiter(StringRedisTemplate redis, ToolsProperties properties) {
        this.redis = redis;
        this.limit = properties.getRateLimitPerMinute();
    }

    public void acquire(String bucket) {
        try {
            String key = "tools:rate:" + bucket + ":" + (Instant.now().getEpochSecond() / 60);
            Long count = redis.opsForValue().increment(key);
            redis.expire(key, Duration.ofMinutes(2));
            if (count != null && count > limit) {
                throw new RateLimitedException("تم الوصول إلى حد استخدام الأدوات لهذه الدقيقة. أعد المحاولة بعد قليل.");
            }
        } catch (RateLimitedException limited) {
            throw limited;
        } catch (RuntimeException exception) {
            log.warn("Tool rate limit skipped");
        }
    }
}
