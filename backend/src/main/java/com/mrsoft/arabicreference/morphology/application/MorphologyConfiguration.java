package com.mrsoft.arabicreference.morphology.application;

import com.mrsoft.arabicreference.morphology.infrastructure.MorphologyProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MorphologyProperties.class)
public class MorphologyConfiguration {
}
