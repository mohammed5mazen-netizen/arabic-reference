package com.mrsoft.arabicreference.shared.infrastructure.security;

import com.mrsoft.arabicreference.shared.kernel.exception.ErrorCode;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.shared.kernel.trace.TraceIds;
import com.mrsoft.arabicreference.shared.web.error.ApiErrorJson;
import com.mrsoft.arabicreference.shared.web.error.ApiErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;

public final class ServletApiErrors {

    private ServletApiErrors() {
    }

    public static void write(HttpServletResponse response, int status, ErrorCode code, String message, TimeProvider timeProvider)
            throws IOException {
        String traceId = TraceIds.current();
        if ("unknown".equals(traceId)) {
            traceId = UUID.randomUUID().toString();
        }
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(TraceIds.HEADER, traceId);
        response.getWriter().write(ApiErrorJson.write(new ApiErrorResponse(
                code.name(),
                message,
                List.of(),
                List.of(),
                traceId,
                timeProvider.now())));
    }
}
