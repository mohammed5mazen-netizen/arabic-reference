package com.mrsoft.arabicreference.tools.infrastructure;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ToolsProperties.class)
public class ToolsConfiguration {
}
