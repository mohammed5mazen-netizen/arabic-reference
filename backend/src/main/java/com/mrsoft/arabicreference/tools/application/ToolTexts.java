package com.mrsoft.arabicreference.tools.application;

import com.mrsoft.arabicreference.linguistics.domain.text.ArabicTextNormalizer;
import com.mrsoft.arabicreference.morphology.domain.ArabicLetters;
import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import com.mrsoft.arabicreference.tools.domain.ToolCatalog;
import com.mrsoft.arabicreference.tools.domain.ToolCode;
import java.util.List;

public final class ToolTexts {

    private static final ArabicTextNormalizer NORMALIZER = new ArabicTextNormalizer();
    public static final String MISSING = "غير متوفر في المرجع حاليًا";
    public static final String INSUFFICIENT = "لا تتوفر في المرجع حاليًا معلومات موثّقة كافية لتحديد الفرق الدلالي.";
    public static final String UNKNOWN_SPELLING = "لم نعثر على هذه الصيغة في البيانات المنشورة.";

    private ToolTexts() {
    }

    public record Prepared(String display, String normalized) {
    }

    public static Prepared word(ToolCode code, String raw) {
        return prepare(code, raw, 1);
    }

    public static Prepared phrase(ToolCode code, String raw) {
        return prepare(code, raw, ToolCatalog.require(code).maxWords());
    }

    private static Prepared prepare(ToolCode code, String raw, int maxWords) {
        ToolCatalog.Definition definition = ToolCatalog.require(code);
        if (raw == null || raw.isBlank()) {
            throw invalid("أدخل نصًا عربيًا.");
        }
        String display = raw.trim();
        int points = display.codePointCount(0, display.length());
        if (points > definition.maxCodePoints()) {
            throw invalid("النص أطول من حد هذه الأداة. الأدوات لا تحلل المقالات الكاملة.");
        }
        String normalized = NORMALIZER.normalize(display).normalizedText();
        if (!ArabicLetters.words(normalized)) {
            throw invalid("أدخل نصًا عربيًا.");
        }
        int words = normalized.isBlank() ? 0 : normalized.split(" ").length;
        if (words > maxWords) {
            throw invalid(maxWords == 1 ? "هذه الأداة تقبل كلمة واحدة." : "النص أطول من حد هذه الأداة. الأدوات لا تحلل المقالات الكاملة.");
        }
        return new Prepared(display, normalized);
    }

    public static String normalize(String value) {
        return value == null || value.isBlank() ? "" : NORMALIZER.normalize(value).normalizedText();
    }

    private static ValidationException invalid(String message) {
        return new ValidationException(message, List.of(new FieldErrorDetail("q", message)));
    }
}
