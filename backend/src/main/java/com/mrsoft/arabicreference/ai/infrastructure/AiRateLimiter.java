package com.mrsoft.arabicreference.ai.infrastructure;

import com.mrsoft.arabicreference.ai.application.AiRateLimitPort;
import com.mrsoft.arabicreference.shared.kernel.exception.RateLimitedException;
import com.mrsoft.arabicreference.shared.kernel.exception.ServiceUnavailableException;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class AiRateLimiter implements AiRateLimitPort {

    private static final Logger log = LoggerFactory.getLogger(AiRateLimiter.class);

    private final StringRedisTemplate redis;
    private final int limit;

    public AiRateLimiter(StringRedisTemplate redis, AiProperties properties) {
        this.redis = redis;
        this.limit = properties.getRateLimitPerMinute();
    }

    @Override
    public void acquire(String bucket) {
        try {
            String key = "ai:rate:" + (bucket == null ? "anonymous" : bucket) + ":" + (Instant.now().getEpochSecond() / 60);
            Long count = redis.opsForValue().increment(key);
            redis.expire(key, Duration.ofMinutes(2));
            if (count != null && count > limit) {
                throw new RateLimitedException("تم الوصول إلى حد استخدام المساعد لهذه الدقيقة. أعد المحاولة بعد قليل.");
            }
        } catch (RateLimitedException limited) {
            throw limited;
        } catch (RuntimeException exception) {
            log.error("Assistant rate limiter is unavailable");
            throw new ServiceUnavailableException("The assistant is temporarily unavailable.");
        }
    }
}
