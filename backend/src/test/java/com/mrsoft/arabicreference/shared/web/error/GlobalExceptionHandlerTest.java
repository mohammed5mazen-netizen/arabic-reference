package com.mrsoft.arabicreference.shared.web.error;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.mrsoft.arabicreference.shared.infrastructure.security.ApiAccessDeniedHandler;
import com.mrsoft.arabicreference.shared.infrastructure.security.ApiAuthenticationEntryPoint;
import com.mrsoft.arabicreference.shared.infrastructure.security.SecurityConfig;
import com.mrsoft.arabicreference.shared.infrastructure.time.UtcTimeProvider;
import com.mrsoft.arabicreference.shared.infrastructure.web.TraceIdFilter;
import com.mrsoft.arabicreference.shared.kernel.exception.ErrorCode;
import com.mrsoft.arabicreference.shared.kernel.security.AccessAuthentication;
import com.mrsoft.arabicreference.shared.kernel.security.AccessTokenAuthenticator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import testprobe.ErrorProbeController;

@WebMvcTest(controllers = ErrorProbeController.class, excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@Import({
        ErrorProbeController.class,
        GlobalExceptionHandler.class,
        UtcTimeProvider.class,
        SecurityConfig.class,
        ApiAuthenticationEntryPoint.class,
        ApiAccessDeniedHandler.class,
        TraceIdFilter.class,
        GlobalExceptionHandlerTest.SliceSecurity.class
})
class GlobalExceptionHandlerTest {

    @TestConfiguration
    static class SliceSecurity {
        @Bean
        AccessTokenAuthenticator accessTokenAuthenticator() {
            return token -> new AccessAuthentication.Rejected(ErrorCode.TOKEN_INVALID, "The access token is invalid.");
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Test
    void notFoundUsesStableErrorShape() throws Exception {
        mockMvc.perform(get("/api/v1/public/__probe/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Missing entry"))
                .andExpect(jsonPath("$.details").isArray())
                .andExpect(jsonPath("$.fieldErrors").isArray())
                .andExpect(jsonPath("$.traceId").isNotEmpty())
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void conflictUsesStableCode() throws Exception {
        mockMvc.perform(get("/api/v1/public/__probe/conflict"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));
    }

    @Test
    void validationReturnsFieldErrors() throws Exception {
        mockMvc.perform(get("/api/v1/public/__probe/validation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("q"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("must not be blank"));
    }

    @Test
    void missingRequiredQueryParameterReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/public/__probe/required-query"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.message").value("معامل البحث مطلوب."))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("q"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("مطلوب"));
    }

    @Test
    void forbiddenUsesStableCode() throws Exception {
        mockMvc.perform(get("/api/v1/public/__probe/forbidden"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN_OPERATION"));
    }

    @Test
    void unexpectedErrorsHideInternalDetails() throws Exception {
        mockMvc.perform(get("/api/v1/public/__probe/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred."))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("sql-leak-marker"))))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("secret_table"))));
    }
}
