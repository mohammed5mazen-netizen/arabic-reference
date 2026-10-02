package com.mrsoft.arabicreference.shared.infrastructure.security;

import com.mrsoft.arabicreference.shared.kernel.exception.ErrorCode;
import com.mrsoft.arabicreference.shared.kernel.security.AccessAuthentication;
import com.mrsoft.arabicreference.shared.kernel.security.AccessTokenAuthenticator;
import com.mrsoft.arabicreference.shared.kernel.security.AuthenticatedAccess;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Authenticates editorial requests that carry a bearer token.
 * Public routes ignore the header so a stale admin token cannot turn a public read into 401.
 */
public class AdminAccessFilter extends OncePerRequestFilter {

    private static final String ADMIN_PREFIX = "/api/v1/admin/";

    private final AccessTokenAuthenticator authenticator;
    private final TimeProvider timeProvider;

    public AdminAccessFilter(AccessTokenAuthenticator authenticator, TimeProvider timeProvider) {
        this.authenticator = authenticator;
        this.timeProvider = timeProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!request.getRequestURI().startsWith(ADMIN_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }
        String header = request.getHeader("Authorization");
        if (header == null || header.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }
        if (!header.startsWith("Bearer ") || header.length() < 8) {
            ServletApiErrors.write(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    ErrorCode.TOKEN_INVALID,
                    "The access token is invalid.",
                    timeProvider);
            return;
        }
        AccessAuthentication result = authenticator.authenticate(header.substring(7).trim());
        if (result instanceof AccessAuthentication.Rejected rejected) {
            int status = rejected.code() == ErrorCode.SERVICE_UNAVAILABLE
                    ? HttpServletResponse.SC_SERVICE_UNAVAILABLE
                    : HttpServletResponse.SC_UNAUTHORIZED;
            ServletApiErrors.write(response, status, rejected.code(), rejected.message(), timeProvider);
            return;
        }
        AuthenticatedAccess access = ((AccessAuthentication.Authenticated) result).access();
        if (access.mustChangePassword() && !passwordChangeAllowed(request)) {
            ServletApiErrors.write(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    ErrorCode.PASSWORD_CHANGE_REQUIRED,
                    "Change the temporary password before using the administration API.",
                    timeProvider);
            return;
        }
        var authorities = access.permissions().stream().map(SimpleGrantedAuthority::new).toList();
        var authentication = UsernamePasswordAuthenticationToken.authenticated(access, "", authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    private static boolean passwordChangeAllowed(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (HttpMethod.GET.matches(request.getMethod()) && path.equals("/api/v1/admin/auth/session")) {
            return true;
        }
        if (!HttpMethod.POST.matches(request.getMethod())) {
            return false;
        }
        return path.equals("/api/v1/admin/auth/change-password") || path.equals("/api/v1/admin/auth/logout");
    }

    static List<String> passwordChangeAllowList() {
        return List.of(
                "GET /api/v1/admin/auth/session",
                "POST /api/v1/admin/auth/change-password",
                "POST /api/v1/admin/auth/logout");
    }
}
