package com.mrsoft.arabicreference.grammar.domain;

import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import java.util.List;

public final class ArabicPhrase {

    private ArabicPhrase() {
    }

    public static String require(String normalized, String field, int max) {
        String value = normalized == null ? "" : normalized.trim();
        int count = value.codePointCount(0, value.length());
        if (count < 1 || count > max || !containsArabic(value)) {
            throw invalid(field, "Enter Arabic text within the allowed length.");
        }
        return value;
    }

    public static boolean containsArabic(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return value.codePoints().anyMatch(ArabicPhrase::isArabicLetter);
    }

    public static boolean isArabicLetter(int codePoint) {
        return (codePoint >= 0x0621 && codePoint <= 0x063A) || (codePoint >= 0x0641 && codePoint <= 0x064A);
    }

    private static ValidationException invalid(String field, String message) {
        return new ValidationException(message, List.of(new FieldErrorDetail(field, message)));
    }
}
