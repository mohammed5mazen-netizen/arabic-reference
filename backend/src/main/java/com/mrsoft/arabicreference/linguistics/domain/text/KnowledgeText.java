package com.mrsoft.arabicreference.linguistics.domain.text;

import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import java.util.List;

public final class KnowledgeText {

    private static final ArabicTextNormalizer NORMALIZER = new ArabicTextNormalizer();

    private KnowledgeText() {
    }

    public static String required(String original, String field, int max) {
        String value = original == null ? "" : original.trim();
        int count = value.codePointCount(0, value.length());
        if (count < 1 || count > max || !containsArabic(value)) {
            throw invalid(field, "Enter Arabic text within the allowed length.");
        }
        return value;
    }

    public static String optional(String original, String field, int max) {
        if (original == null || original.isBlank()) {
            return null;
        }
        return required(original, field, max);
    }

    public static String plain(String original, String field, int max) {
        if (original == null || original.isBlank()) {
            return null;
        }
        String value = original.trim();
        if (value.codePointCount(0, value.length()) > max) {
            throw invalid(field, "Enter text within the allowed length.");
        }
        return value;
    }

    public static String normalized(String original) {
        return NORMALIZER.normalize(original).normalizedText();
    }

    public static boolean containsArabic(String value) {
        return value != null && value.codePoints().anyMatch(KnowledgeText::isArabicLetter);
    }

    public static boolean isArabicLetter(int codePoint) {
        return (codePoint >= 0x0621 && codePoint <= 0x063A) || (codePoint >= 0x0641 && codePoint <= 0x064A);
    }

    private static ValidationException invalid(String field, String message) {
        return new ValidationException(message, List.of(new FieldErrorDetail(field, message)));
    }
}
