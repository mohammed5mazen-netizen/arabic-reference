package com.mrsoft.arabicreference.ai.infrastructure;

import com.mrsoft.arabicreference.ai.application.AiModelPort;
import com.mrsoft.arabicreference.ai.application.AiProviderException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiConfiguration {

    @Bean
    @ConditionalOnProperty(name = "app.ai.provider", havingValue = "none", matchIfMissing = true)
    AiModelPort unavailableAiModel() {
        return request -> {
            throw new AiProviderException(AiProviderException.Kind.UNAVAILABLE);
        };
    }
}
