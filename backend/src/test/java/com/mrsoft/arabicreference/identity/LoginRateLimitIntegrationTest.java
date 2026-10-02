package com.mrsoft.arabicreference.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mrsoft.arabicreference.IntegrationContainers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class LoginRateLimitIntegrationTest {

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        IntegrationContainers.register(registry);
        registry.add("app.admin.rate-limit.login", () -> "2");
        registry.add("app.admin.rate-limit.window", () -> "1m");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StringRedisTemplate redis;

    @BeforeEach
    void clearLoginCounter() {
        redis.delete("admin:auth:rl:login:127.0.0.1");
    }

    @Test
    void loginIsRateLimitedThroughRedis() throws Exception {
        String body = """
                {"username":"rate.user","password":"Wrong-Pass-123!"}
                """;
        mockMvc.perform(post("/api/v1/admin/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
        mockMvc.perform(post("/api/v1/admin/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/v1/admin/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("RATE_LIMITED"));
        assertThat(redis.opsForValue().get("admin:auth:rl:login:127.0.0.1")).isEqualTo("3");
    }
}
