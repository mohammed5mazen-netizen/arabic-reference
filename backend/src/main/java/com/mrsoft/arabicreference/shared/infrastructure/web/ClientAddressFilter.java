package com.mrsoft.arabicreference.shared.infrastructure.web;

import com.mrsoft.arabicreference.shared.web.ClientAddresses;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class ClientAddressFilter extends OncePerRequestFilter {

    private final Set<String> trustedProxies;

    public ClientAddressFilter(@Value("${app.security.trusted-proxies:}") String trustedProxies) {
        this.trustedProxies = ClientAddresses.parse(trustedProxies);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        request.setAttribute(
                ClientAddresses.ATTRIBUTE,
                ClientAddresses.choose(request.getRemoteAddr(), request.getHeader("X-Forwarded-For"), trustedProxies));
        filterChain.doFilter(request, response);
    }
}
