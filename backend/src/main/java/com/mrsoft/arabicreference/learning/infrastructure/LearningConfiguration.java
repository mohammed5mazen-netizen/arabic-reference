package com.mrsoft.arabicreference.learning.infrastructure;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(LearningProperties.class)
public class LearningConfiguration {
}
