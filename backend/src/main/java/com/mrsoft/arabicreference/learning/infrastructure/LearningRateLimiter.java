package com.mrsoft.arabicreference.learning.infrastructure;

import com.mrsoft.arabicreference.shared.kernel.exception.RateLimitedException;
import com.mrsoft.arabicreference.shared.kernel.exception.ServiceUnavailableException;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class LearningRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(LearningRateLimiter.class);

    private final StringRedisTemplate redis;
    private final int limit;

    public LearningRateLimiter(StringRedisTemplate redis, LearningProperties properties) {
        this.redis = redis;
        this.limit = properties.rateLimitPerMinute();
    }

    public void acquire(String bucket) {
        try {
            String key = "learning:rate:" + (bucket == null ? "anonymous" : bucket) + ":" + (Instant.now().getEpochSecond() / 60);
            Long count = redis.opsForValue().increment(key);
            redis.expire(key, Duration.ofMinutes(2));
            if (count != null && count > limit) {
                throw new RateLimitedException("تم الوصول إلى حد محاولات الاختبار لهذه الدقيقة. أعد المحاولة بعد قليل.");
            }
        } catch (RateLimitedException limited) {
            throw limited;
        } catch (RuntimeException exception) {
            log.error("Learning rate limiter is unavailable");
            throw new ServiceUnavailableException("Quiz attempts are temporarily unavailable.");
        }
    }
}
