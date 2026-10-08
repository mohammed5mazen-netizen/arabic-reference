package com.mrsoft.arabicreference.ai.application;

import com.mrsoft.arabicreference.linguistics.domain.text.ArabicTextNormalizer;
import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import java.util.List;

public final class AiTexts {

    public static final String INSUFFICIENT = "لا تتوفر في المرجع حاليًا معلومات موثقة كافية للإجابة عن هذا السؤال.";
    public static final String DISABLED = "المساعد الذكي غير متاح حاليًا. يمكنك استخدام البحث والأدوات اللغوية.";
    public static final String TIMEOUT = "تعذر إكمال الإجابة الآن. جرّب مرة أخرى بعد قليل أو استخدم البحث.";
    public static final String PROVIDER_FAILURE = "تعذر إكمال الإجابة الآن. جرّب مرة أخرى بعد قليل أو استخدم البحث.";
    public static final String PARSING_LIMIT = "الإعراب الآلي غير مدعوم. البحث متاح في القواعد والأمثلة المنشورة فقط.";
    public static final String RULE_LIMIT = "يتضمن التحليل الصرفي احتمالًا مستنتجًا بقاعدة، وليس حقيقة قطعية.";
    public static final String COMPARISON_LIMIT = "لا تتوفر في المرجع حاليًا معلومات موثقة كافية لتحديد الفرق الدلالي.";
    public static final String MISSING_SIDE = "أحد طرفي المقارنة غير موجود في المعرفة المنشورة.";

    private static final ArabicTextNormalizer NORMALIZER = new ArabicTextNormalizer();

    private AiTexts() {
    }

    public record Prepared(String display, String normalized) {
    }

    public static Prepared prepare(String raw, int maxCodePoints, int maxLines) {
        if (raw == null || raw.isBlank()) {
            throw invalid("أدخل سؤالًا عربيًا.");
        }
        String display = raw.trim();
        int lines = display.split("\\R", -1).length;
        if (lines > maxLines) {
            throw invalid("السؤال أطول من الحد المسموح. المساعد لا يقرأ كتابًا كاملًا.");
        }
        if (display.codePointCount(0, display.length()) > maxCodePoints) {
            throw invalid("السؤال أطول من الحد المسموح. المساعد لا يقرأ كتابًا كاملًا.");
        }
        if (!containsArabic(display)) {
            throw invalid("أدخل سؤالًا بالعربية.");
        }
        return new Prepared(display, NORMALIZER.normalize(display).normalizedText());
    }

    public static String plain(String value, int maxCodePoints) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String stripped = value.replaceAll("(?i)<[^>]*>", " ")
                .replace("&lt;", " ")
                .replace("&gt;", " ")
                .replace("&amp;", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (stripped.codePointCount(0, stripped.length()) <= maxCodePoints) {
            return stripped;
        }
        int end = stripped.offsetByCodePoints(0, maxCodePoints);
        return stripped.substring(0, end).trim();
    }

    public static boolean containsArabic(String value) {
        return value.codePoints().anyMatch(code -> (code >= 0x0621 && code <= 0x063A) || (code >= 0x0641 && code <= 0x064A));
    }

    private static ValidationException invalid(String message) {
        return new ValidationException(message, List.of(new FieldErrorDetail("question", message)));
    }
}
