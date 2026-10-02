package com.mrsoft.arabicreference.shared.infrastructure.security;

import com.mrsoft.arabicreference.shared.kernel.exception.ErrorCode;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class ApiAccessDeniedHandler implements AccessDeniedHandler {

    static final String MESSAGE = "You do not have permission to perform this operation.";

    private final TimeProvider timeProvider;

    public ApiAccessDeniedHandler(TimeProvider timeProvider) {
        this.timeProvider = timeProvider;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {
        ServletApiErrors.write(response, HttpServletResponse.SC_FORBIDDEN, ErrorCode.FORBIDDEN_OPERATION, MESSAGE, timeProvider);
    }
}
