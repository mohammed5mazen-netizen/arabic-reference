package com.mrsoft.arabicreference.ai.infrastructure;

import com.mrsoft.arabicreference.ai.application.AiPropertiesView;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai")
public class AiProperties implements AiPropertiesView {

    private boolean enabled;
    private String provider = "none";
    private String model = "";
    private String apiKey = "";
    private String baseUrl = "https://api.openai.com/v1/chat/completions";
    private Duration timeout = Duration.ofSeconds(20);
    private int maxOutputTokens = 400;
    private int maxContextItems = 6;
    private int maxExcerptChars = 280;
    private int maxQuestionCodePoints = 400;
    private int maxQuestionLines = 8;
    private int rateLimitPerMinute = 12;
    private boolean cacheEnabled = true;
    private Duration cacheTtl = Duration.ofMinutes(10);

    @Override
    public boolean operational() {
        if (!enabled) {
            return false;
        }
        if ("stub".equalsIgnoreCase(provider)) {
            return true;
        }
        return "openai-compatible".equalsIgnoreCase(provider) && apiKey != null && !apiKey.isBlank();
    }

    @Override
    public boolean cacheEnabled() {
        return cacheEnabled;
    }

    @Override
    public boolean keyConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    @Override
    public String provider() {
        return provider == null ? "none" : provider;
    }

    @Override
    public String model() {
        return model == null || model.isBlank() ? "unconfigured" : model;
    }

    @Override
    public int maxQuestionCodePoints() {
        return Math.min(Math.max(maxQuestionCodePoints, 40), 800);
    }

    @Override
    public int maxQuestionLines() {
        return Math.min(Math.max(maxQuestionLines, 1), 12);
    }

    @Override
    public int maxContextItems() {
        return Math.min(Math.max(maxContextItems, 1), 8);
    }

    @Override
    public int maxExcerptChars() {
        return Math.min(Math.max(maxExcerptChars, 80), 400);
    }

    @Override
    public int maxOutputTokens() {
        return Math.min(Math.max(maxOutputTokens, 64), 800);
    }

    @Override
    public int totalBudget() {
        return maxExcerptChars() * Math.min(maxContextItems(), 4);
    }

    public Duration getTimeout() {
        return timeout == null ? Duration.ofSeconds(20) : timeout;
    }

    public int getRateLimitPerMinute() {
        return Math.min(Math.max(rateLimitPerMinute, 1), 60);
    }

    public Duration getCacheTtl() {
        return cacheTtl == null ? Duration.ofMinutes(10) : cacheTtl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getApiKey() {
        return apiKey == null ? "" : apiKey;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public void setMaxOutputTokens(int maxOutputTokens) {
        this.maxOutputTokens = maxOutputTokens;
    }

    public void setMaxContextItems(int maxContextItems) {
        this.maxContextItems = maxContextItems;
    }

    public void setMaxExcerptChars(int maxExcerptChars) {
        this.maxExcerptChars = maxExcerptChars;
    }

    public void setMaxQuestionCodePoints(int maxQuestionCodePoints) {
        this.maxQuestionCodePoints = maxQuestionCodePoints;
    }

    public void setMaxQuestionLines(int maxQuestionLines) {
        this.maxQuestionLines = maxQuestionLines;
    }

    public void setRateLimitPerMinute(int rateLimitPerMinute) {
        this.rateLimitPerMinute = rateLimitPerMinute;
    }

    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    public void setCacheTtl(Duration cacheTtl) {
        this.cacheTtl = cacheTtl;
    }
}
