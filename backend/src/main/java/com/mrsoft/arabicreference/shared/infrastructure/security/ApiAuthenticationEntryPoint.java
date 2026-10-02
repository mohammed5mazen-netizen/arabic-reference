package com.mrsoft.arabicreference.shared.infrastructure.security;

import com.mrsoft.arabicreference.shared.kernel.exception.ErrorCode;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.shared.kernel.trace.TraceIds;
import com.mrsoft.arabicreference.shared.web.error.ApiErrorJson;
import com.mrsoft.arabicreference.shared.web.error.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    static final String MESSAGE = "Authentication is required for this operation.";

    private final TimeProvider timeProvider;

    public ApiAuthenticationEntryPoint(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        String traceId = TraceIds.current();
        if ("unknown".equals(traceId)) {
            traceId = java.util.UUID.randomUUID().toString();
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(TraceIds.HEADER, traceId);
        ApiErrorResponse body = new ApiErrorResponse(
                ErrorCode.UNAUTHORIZED.name(),
                MESSAGE,
                List.of(),
                List.of(),
                traceId,
                timeProvider.now());
        response.getWriter().write(ApiErrorJson.write(body));
    }
}
