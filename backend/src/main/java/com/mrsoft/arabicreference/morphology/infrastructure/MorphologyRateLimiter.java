package com.mrsoft.arabicreference.morphology.infrastructure;

import com.mrsoft.arabicreference.shared.kernel.exception.RateLimitedException;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * A generous per-minute ceiling for anonymous analysis. Redis failure does not block the public tool.
 */
@Component
public class MorphologyRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(MorphologyRateLimiter.class);

    private final StringRedisTemplate redis;
    private final int limit;

    public MorphologyRateLimiter(StringRedisTemplate redis, MorphologyProperties properties) {
        this.redis = redis;
        this.limit = properties.getRateLimitPerMinute();
    }

    public void acquire(String bucket) {
        try {
            String key = "morphology:rate:" + bucket + ":" + (Instant.now().getEpochSecond() / 60);
            Long count = redis.opsForValue().increment(key);
            redis.expire(key, Duration.ofMinutes(2));
            if (count != null && count > limit) {
                throw new RateLimitedException("Morphology analysis is temporarily limited.");
            }
        } catch (RateLimitedException limited) {
            throw limited;
        } catch (RuntimeException exception) {
            log.warn("Morphology rate limit skipped");
        }
    }
}
