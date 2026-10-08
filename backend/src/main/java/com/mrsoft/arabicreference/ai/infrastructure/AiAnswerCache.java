package com.mrsoft.arabicreference.ai.infrastructure;

import com.mrsoft.arabicreference.ai.application.AiAnswerCachePort;
import com.mrsoft.arabicreference.ai.application.AiViews.AssistantAnswer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class AiAnswerCache implements AiAnswerCachePort {

    private static final Logger log = LoggerFactory.getLogger(AiAnswerCache.class);

    private final StringRedisTemplate redis;
    private final JsonMapper json;
    private final AiProperties properties;

    public AiAnswerCache(StringRedisTemplate redis, JsonMapper json, AiProperties properties) {
        this.redis = redis;
        this.json = json;
        this.properties = properties;
    }

    @Override
    public AssistantAnswer read(String key) {
        try {
            String cached = redis.opsForValue().get(key);
            return cached == null ? null : json.readValue(cached, AssistantAnswer.class);
        } catch (RuntimeException exception) {
            log.warn("Assistant cache read skipped");
            return null;
        }
    }

    @Override
    public void write(String key, AssistantAnswer answer) {
        try {
            redis.opsForValue().set(key, json.writeValueAsString(answer), properties.getCacheTtl());
        } catch (RuntimeException exception) {
            log.warn("Assistant cache write skipped");
        }
    }
}
