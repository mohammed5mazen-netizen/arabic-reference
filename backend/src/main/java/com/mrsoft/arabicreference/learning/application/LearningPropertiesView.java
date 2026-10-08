package com.mrsoft.arabicreference.learning.application;

import java.time.Duration;

public interface LearningPropertiesView {

    Duration attemptTtl();

    int rateLimitPerMinute();
}
