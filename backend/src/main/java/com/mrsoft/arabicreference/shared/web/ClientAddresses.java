package com.mrsoft.arabicreference.shared.web;

import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Uses the socket address unless the immediate peer is a configured trusted proxy.
 * A client on the public internet cannot supply its own X-Forwarded-For value.
 */
public final class ClientAddresses {

    public static final String ATTRIBUTE = "arabic.clientAddress";

    private ClientAddresses() {
    }

    public static Set<String> parse(String raw) {
        Set<String> values = new LinkedHashSet<>();
        if (raw == null || raw.isBlank()) {
            return values;
        }
        for (String part : raw.split(",")) {
            String address = sanitize(part);
            if (address != null) {
                values.add(address);
            }
        }
        return values;
    }

    public static String choose(String remoteAddr, String forwardedFor, Set<String> trustedProxies) {
        String remote = sanitize(remoteAddr);
        if (remote == null) {
            return "unknown";
        }
        if (trustedProxies == null || !trustedProxies.contains(remote)) {
            return remote;
        }
        if (forwardedFor == null || forwardedFor.isBlank()) {
            return remote;
        }
        String client = sanitize(forwardedFor.split(",")[0]);
        return client == null ? remote : client;
    }

    public static String read(HttpServletRequest request) {
        Object current = request.getAttribute(ATTRIBUTE);
        if (current instanceof String text && !text.isBlank()) {
            return text;
        }
        String remote = sanitize(request.getRemoteAddr());
        return remote == null ? "unknown" : remote;
    }

    public static String sanitize(String value) {
        if (value == null) {
            return null;
        }
        StringBuilder out = new StringBuilder();
        value.trim().codePoints().limit(64).forEach(codePoint -> {
            if (codePoint >= 32 && codePoint != 127) {
                out.appendCodePoint(codePoint);
            }
        });
        String text = out.toString();
        return text.isEmpty() ? null : text;
    }
}
