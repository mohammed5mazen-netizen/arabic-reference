package com.mrsoft.arabicreference.identity.domain;

import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class Emails {

    private static final Pattern PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private Emails() {
    }

    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            throw invalid();
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        if (normalized.length() > 320 || !PATTERN.matcher(normalized).matches()) {
            throw invalid();
        }
        return normalized;
    }

    private static ValidationException invalid() {
        return new ValidationException(
                "Email is invalid.",
                List.of(new FieldErrorDetail("email", "Enter a valid email address.")));
    }
}
