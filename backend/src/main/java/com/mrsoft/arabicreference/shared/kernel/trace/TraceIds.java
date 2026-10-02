package com.mrsoft.arabicreference.shared.kernel.trace;

import java.util.UUID;
import java.util.regex.Pattern;
import org.slf4j.MDC;

public final class TraceIds {

    public static final String HEADER = "X-Trace-Id";
    public static final String MDC_KEY = "traceId";

    private static final Pattern CANONICAL_UUID = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    private TraceIds() {
    }

    public static boolean isValid(String candidate) {
        return candidate != null && CANONICAL_UUID.matcher(candidate).matches();
    }

    public static String current() {
        String value = MDC.get(MDC_KEY);
        return isValid(value) ? value : "unknown";
    }
}
