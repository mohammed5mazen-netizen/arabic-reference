package com.mrsoft.arabicreference.identity.domain;

import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public final class Usernames {

    private static final Pattern PATTERN = Pattern.compile("^[a-z0-9][a-z0-9._-]{2,31}$");

    private Usernames() {
    }

    public static String normalize(String raw) {
        if (raw == null || raw.isBlank()) {
            throw invalid();
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        if (!PATTERN.matcher(normalized).matches()) {
            throw invalid();
        }
        return normalized;
    }

    private static ValidationException invalid() {
        return new ValidationException(
                "Username is invalid.",
                List.of(new FieldErrorDetail(
                        "username",
                        "Use 3 to 32 characters: letters, digits, dots, underscores, or hyphens.")));
    }
}
