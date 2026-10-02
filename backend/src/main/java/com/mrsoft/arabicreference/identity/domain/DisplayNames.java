package com.mrsoft.arabicreference.identity.domain;

import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import java.util.List;

public final class DisplayNames {

    private DisplayNames() {
    }

    public static String normalize(String raw) {
        if (raw == null) {
            throw invalid();
        }
        String normalized = raw.trim();
        if (normalized.length() < 2 || normalized.length() > 120 || normalized.chars().anyMatch(Character::isISOControl)) {
            throw invalid();
        }
        return normalized;
    }

    private static ValidationException invalid() {
        return new ValidationException(
                "Display name is invalid.",
                List.of(new FieldErrorDetail("displayName", "Use 2 to 120 visible characters.")));
    }
}
