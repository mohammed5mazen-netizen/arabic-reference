package com.mrsoft.arabicreference.tools.infrastructure;

import com.mrsoft.arabicreference.tools.application.ToolViews.ToolEnvelope;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * One cache for every deterministic tool. A Redis failure leaves the request uncached.
 */
@Component
public class ToolResultCache {

    private static final Logger log = LoggerFactory.getLogger(ToolResultCache.class);

    private final StringRedisTemplate redis;
    private final JsonMapper json;
    private final Duration ttl;

    public ToolResultCache(StringRedisTemplate redis, JsonMapper json, ToolsProperties properties) {
        this.redis = redis;
        this.json = json;
        this.ttl = properties.getCacheTtl();
    }

    public ToolEnvelope read(String key) {
        try {
            String cached = redis.opsForValue().get(key);
            return cached == null ? null : json.readValue(cached, ToolEnvelope.class);
        } catch (RuntimeException exception) {
            log.warn("Tool cache read skipped");
            return null;
        }
    }

    public void write(String key, ToolEnvelope envelope) {
        try {
            redis.opsForValue().set(key, json.writeValueAsString(envelope), ttl);
        } catch (RuntimeException exception) {
            log.warn("Tool cache write skipped");
        }
    }
}
