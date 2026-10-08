package com.mrsoft.arabicreference.ai;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mrsoft.arabicreference.IntegrationContainers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AiDisabledIntegrationTest {

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        IntegrationContainers.register(registry);
        registry.add("app.admin.bootstrap.username", () -> "owner");
        registry.add("app.admin.bootstrap.email", () -> "owner@arabic-reference.test");
        registry.add("app.admin.bootstrap.display-name", () -> "Platform Owner");
        registry.add("app.admin.bootstrap.password", () -> "Owner-Pass-123!");
        registry.add("app.ai.enabled", () -> "false");
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void disabledAssistantLeavesTheReferenceAvailable() throws Exception {
        mockMvc.perform(get("/api/v1/public/ai/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.available").value(false))
                .andExpect(jsonPath("$.data.message").value(org.hamcrest.Matchers.containsString("غير متاح")));
        mockMvc.perform(post("/api/v1/public/ai/ask").contentType("application/json").content("{\"question\":\"ما معنى كتاب؟\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("غير متاح")));
        mockMvc.perform(get("/api/v1/public/tools")).andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/public/grammar/topics").param("page", "0").param("size", "1")).andExpect(status().isOk());
    }
}
