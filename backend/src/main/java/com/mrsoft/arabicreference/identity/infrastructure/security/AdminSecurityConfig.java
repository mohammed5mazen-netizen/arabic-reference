package com.mrsoft.arabicreference.identity.infrastructure.security;

import com.mrsoft.arabicreference.identity.application.AdminSecurityProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableConfigurationProperties(AdminSecurityProperties.class)
public class AdminSecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(AdminSecurityConfig.class);
    static final String LOCAL_JWT_PLACEHOLDER = "local-dev-only-admin-jwt-secret-key-32b";

    @Bean
    PasswordEncoder passwordEncoder() {
        return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }

    @Bean
    AdminSecretWarnings adminSecretWarnings(AdminSecurityProperties properties) {
        if (LOCAL_JWT_PLACEHOLDER.equals(properties.getJwtSecret())) {
            log.warn("ADMIN_JWT_SECRET is the local development placeholder. Override it outside local development.");
        }
        return new AdminSecretWarnings();
    }

    static final class AdminSecretWarnings {
    }
}
