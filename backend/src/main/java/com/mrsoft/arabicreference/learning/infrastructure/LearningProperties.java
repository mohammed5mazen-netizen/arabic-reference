package com.mrsoft.arabicreference.learning.infrastructure;

import com.mrsoft.arabicreference.learning.application.LearningPropertiesView;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.learning")
public class LearningProperties implements LearningPropertiesView {

    private Duration attemptTtl = Duration.ofMinutes(60);
    private int rateLimitPerMinute = 20;

    @Override
    public Duration attemptTtl() {
        long minutes = attemptTtl == null ? 60 : attemptTtl.toMinutes();
        if (minutes < 30) {
            return Duration.ofMinutes(30);
        }
        if (minutes > 120) {
            return Duration.ofMinutes(120);
        }
        return Duration.ofMinutes(minutes);
    }

    @Override
    public int rateLimitPerMinute() {
        return Math.min(Math.max(rateLimitPerMinute, 5), 60);
    }

    public void setAttemptTtl(Duration attemptTtl) {
        this.attemptTtl = attemptTtl;
    }

    public void setRateLimitPerMinute(int rateLimitPerMinute) {
        this.rateLimitPerMinute = rateLimitPerMinute;
    }
}
