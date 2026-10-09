package com.mrsoft.arabicreference.identity.infrastructure.security;

import com.mrsoft.arabicreference.identity.application.AdminSecurityProperties;
import com.mrsoft.arabicreference.shared.kernel.exception.RateLimitedException;
import com.mrsoft.arabicreference.shared.kernel.exception.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisAuthRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(RedisAuthRateLimiter.class);

    private final StringRedisTemplate redis;
    private final AdminSecurityProperties properties;

    public RedisAuthRateLimiter(StringRedisTemplate redis, AdminSecurityProperties properties) {
        this.redis = redis;
        this.properties = properties;
    }

    public void checkLogin(String clientAddress) {
        hit("login", clientAddress, properties.getRateLimit().getLogin());
    }

    public void checkRefresh(String clientAddress) {
        hit("refresh", clientAddress, properties.getRateLimit().getRefresh());
    }

    public void checkPassword(String clientAddress) {
        hit("password", clientAddress, properties.getRateLimit().getPassword());
    }

    private void hit(String action, String clientAddress, int limit) {
        String address = clientAddress == null || clientAddress.isBlank() ? "unknown" : clientAddress;
        String key = "admin:auth:rl:" + action + ":" + address;
        try {
            Long count = redis.opsForValue().increment(key);
            if (count != null && count == 1L) {
                redis.expire(key, properties.getRateLimit().getWindow());
            }
            if (count != null && count > limit) {
                throw new RateLimitedException("Too many attempts. Try again later.");
            }
        } catch (RateLimitedException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            log.error("Admin auth rate limiter is unavailable");
            throw new ServiceUnavailableException("Authentication is temporarily unavailable.");
        }
    }
}
