package com.mrsoft.arabicreference.identity.infrastructure.security;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisAccessTokenDenylist {

    private static final Logger log = LoggerFactory.getLogger(RedisAccessTokenDenylist.class);

    private final StringRedisTemplate redis;

    public RedisAccessTokenDenylist(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public void revoke(String tokenId, Duration ttl) {
        if (tokenId == null || ttl == null || ttl.isZero() || ttl.isNegative()) {
            return;
        }
        try {
            redis.opsForValue().set(key(tokenId), "1", ttl);
        } catch (RuntimeException exception) {
            log.warn("Could not revoke an access token before it expires");
        }
    }

    public boolean isRevoked(String tokenId) {
        try {
            return Boolean.TRUE.equals(redis.hasKey(key(tokenId)));
        } catch (RuntimeException exception) {
            log.error("Admin access-token denylist is unavailable");
            throw exception;
        }
    }

    private static String key(String tokenId) {
        return "admin:access:deny:" + tokenId;
    }
}
