package com.mrsoft.arabicreference.ai;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mrsoft.arabicreference.IntegrationContainers;
import com.mrsoft.arabicreference.ai.application.AiModelPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(AiRateLimitIntegrationTest.StubConfig.class)
class AiRateLimitIntegrationTest {

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        IntegrationContainers.register(registry);
        registry.add("app.admin.bootstrap.username", () -> "owner");
        registry.add("app.admin.bootstrap.email", () -> "owner@arabic-reference.test");
        registry.add("app.admin.bootstrap.display-name", () -> "Platform Owner");
        registry.add("app.admin.bootstrap.password", () -> "Owner-Pass-123!");
        registry.add("app.ai.enabled", () -> "true");
        registry.add("app.ai.provider", () -> "stub");
        registry.add("app.ai.cache-enabled", () -> "false");
        registry.add("app.ai.rate-limit-per-minute", () -> "2");
    }

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void resetModel() {
        ScriptedAiModel.reset();
    }

    @Test
    void assistantReturns429WhenTheMinuteCeilingIsExceeded() throws Exception {
        String client = "10.9.8.8";
        ask(client);
        ask(client);
        mockMvc.perform(post("/api/v1/public/ai/ask").header("X-Forwarded-For", client).contentType(MediaType.APPLICATION_JSON).content("{\"question\":\"ما معنى كتاب؟\"}"))
                .andExpect(status().isTooManyRequests());
    }

    private void ask(String client) throws Exception {
        mockMvc.perform(post("/api/v1/public/ai/ask").header("X-Forwarded-For", client).contentType(MediaType.APPLICATION_JSON).content("{\"question\":\"ما معنى كتاب؟\"}"))
                .andExpect(status().isOk());
    }

    @TestConfiguration
    static class StubConfig {
        @Bean
        AiModelPort scriptedAiModel() {
            return ScriptedAiModel.INSTANCE;
        }
    }
}
