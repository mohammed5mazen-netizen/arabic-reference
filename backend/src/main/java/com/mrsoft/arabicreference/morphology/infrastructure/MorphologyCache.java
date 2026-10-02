package com.mrsoft.arabicreference.morphology.infrastructure;

import com.mrsoft.arabicreference.morphology.domain.AnalysisReport;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Optional cache for deterministic public analyses. A Redis failure leaves the request uncached.
 */
@Component
public class MorphologyCache {

    private static final Logger log = LoggerFactory.getLogger(MorphologyCache.class);

    private final StringRedisTemplate redis;
    private final JsonMapper json;
    private final Duration ttl;

    public MorphologyCache(StringRedisTemplate redis, JsonMapper json, MorphologyProperties properties) {
        this.redis = redis;
        this.json = json;
        this.ttl = properties.getCacheTtl();
    }

    public AnalysisReport read(String key) {
        try {
            String cached = redis.opsForValue().get(key);
            return cached == null ? null : json.readValue(cached, AnalysisReport.class);
        } catch (RuntimeException exception) {
            log.warn("Morphology cache read skipped");
            return null;
        }
    }

    public void write(String key, AnalysisReport report) {
        try {
            redis.opsForValue().set(key, json.writeValueAsString(report), ttl);
        } catch (RuntimeException exception) {
            log.warn("Morphology cache write skipped");
        }
    }
}
