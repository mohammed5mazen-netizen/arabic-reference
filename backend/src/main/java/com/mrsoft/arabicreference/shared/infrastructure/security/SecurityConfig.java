package com.mrsoft.arabicreference.shared.infrastructure.security;

import com.mrsoft.arabicreference.shared.kernel.security.AccessTokenAuthenticator;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.shared.kernel.trace.TraceIds;
import java.util.List;
import com.mrsoft.arabicreference.shared.infrastructure.web.ClientAddressFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Public linguistic reads stay anonymous. Editorial authentication is a bearer token for
 * /api/v1/admin/** and never a visitor session.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final String frontendUrl;
    private final ApiAuthenticationEntryPoint authenticationEntryPoint;
    private final ApiAccessDeniedHandler accessDeniedHandler;
    private final AdminAccessFilter adminAccessFilter;
    private final boolean hsts;

    public SecurityConfig(
            @Value("${app.frontend-url:http://localhost:3000}") String frontendUrl,
            @Value("${app.security.hsts:false}") boolean hsts,
            ApiAuthenticationEntryPoint authenticationEntryPoint,
            ApiAccessDeniedHandler accessDeniedHandler,
            AccessTokenAuthenticator accessTokenAuthenticator,
            TimeProvider timeProvider) {
        this.frontendUrl = frontendUrl;
        this.hsts = hsts;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
        this.adminAccessFilter = new AdminAccessFilter(accessTokenAuthenticator, timeProvider);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, ClientAddressFilter clientAddresses) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .headers(headers -> {
                    headers.contentTypeOptions(Customizer.withDefaults())
                            .frameOptions(frame -> frame.deny())
                            .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                            .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'none'; frame-ancestors 'none'; base-uri 'none'"))
                            .permissionsPolicyHeader(permissions -> permissions.policy("camera=(), microphone=(), geolocation=()"));
                    if (hsts) {
                        headers.httpStrictTransportSecurity(transport -> transport.includeSubDomains(true).maxAgeInSeconds(15_552_000));
                    } else {
                        headers.httpStrictTransportSecurity(transport -> transport.disable());
                    }
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .requestCache(cache -> cache.disable())
                .exceptionHandling(handler -> handler
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/public/**").permitAll()
                        .requestMatchers(HttpMethod.HEAD, "/api/v1/public/**").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/api/v1/public/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/public/ai/ask").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/public/learning/quizzes/*/attempts").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/public/learning/attempts/*/submit").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/admin/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/admin/auth/refresh").permitAll()
                        .requestMatchers("/api/v1/admin/**").authenticated()
                        .requestMatchers("/api/**").authenticated()
                        .requestMatchers("/actuator/**").denyAll()
                        .anyRequest().permitAll())
                .addFilterBefore(clientAddresses, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(adminAccessFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    FilterRegistrationBean<ClientAddressFilter> clientAddressRegistration(ClientAddressFilter clientAddresses) {
        FilterRegistrationBean<ClientAddressFilter> registration = new FilterRegistrationBean<>(clientAddresses);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    FilterRegistrationBean<AdminAccessFilter> adminAccessFilterRegistration() {
        FilterRegistrationBean<AdminAccessFilter> registration = new FilterRegistrationBean<>(adminAccessFilter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration publicReads = new CorsConfiguration();
        publicReads.setAllowedOrigins(List.of(frontendUrl));
        publicReads.setAllowedMethods(List.of("GET", "HEAD", "OPTIONS"));
        publicReads.setAllowedHeaders(List.of("Accept", "Content-Type", TraceIds.HEADER));
        publicReads.setExposedHeaders(List.of(TraceIds.HEADER));
        publicReads.setAllowCredentials(false);

        CorsConfiguration adminApi = new CorsConfiguration();
        adminApi.setAllowedOrigins(List.of(frontendUrl));
        adminApi.setAllowedMethods(List.of("GET", "POST", "PATCH", "PUT", "OPTIONS"));
        adminApi.setAllowedHeaders(List.of("Accept", "Content-Type", "Authorization", TraceIds.HEADER));
        adminApi.setExposedHeaders(List.of(TraceIds.HEADER));
        adminApi.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/v1/public/**", publicReads);
        source.registerCorsConfiguration("/api/v1/admin/**", adminApi);
        return source;
    }
}
