package com.mrsoft.arabicreference.shared.infrastructure.security;

import com.mrsoft.arabicreference.shared.kernel.exception.ErrorCode;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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
        ServletApiErrors.write(response, HttpServletResponse.SC_UNAUTHORIZED, ErrorCode.UNAUTHORIZED, MESSAGE, timeProvider);
    }
}
