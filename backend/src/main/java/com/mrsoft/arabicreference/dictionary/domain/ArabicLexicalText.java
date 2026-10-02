package com.mrsoft.arabicreference.dictionary.domain;

import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import java.util.List;

/**
 * Letter checks for lemmas and roots after Arabic normalization.
 * Triliteral and quadriliteral roots are the normal cases. Lengths 2, 5, and 6 need an editorial note.
 */
public final class ArabicLexicalText {

    private ArabicLexicalText() {
    }

    public static String requireLemma(String normalized) {
        String value = normalized == null ? "" : normalized.trim();
        if (value.isEmpty() || value.codePointCount(0, value.length()) > 80 || !arabicWords(value)) {
            throw invalid("lemma", "Enter an Arabic lemma of 1 to 80 characters.");
        }
        return value;
    }

    public static String requireRoot(String lettersOnly, String notes) {
        String letters = lettersOnly == null ? "" : lettersOnly;
        int count = letters.codePointCount(0, letters.length());
        if (!arabicLetters(letters) || count < 2 || count > 6) {
            throw invalid("root", "A root must be Arabic letters. Triliteral and quadriliteral roots are the normal forms.");
        }
        if ((count < 3 || count > 4) && (notes == null || notes.isBlank())) {
            throw invalid("notes", "Roots that are not triliteral or quadriliteral need a short editorial note.");
        }
        return letters;
    }

    public static boolean isArabicLetter(int codePoint) {
        return (codePoint >= 0x0621 && codePoint <= 0x063A) || (codePoint >= 0x0641 && codePoint <= 0x064A);
    }

    private static boolean arabicWords(String value) {
        boolean letter = false;
        for (int index = 0; index < value.length();) {
            int codePoint = value.codePointAt(index);
            index += Character.charCount(codePoint);
            if (codePoint == ' ') {
                continue;
            }
            if (!isArabicLetter(codePoint)) {
                return false;
            }
            letter = true;
        }
        return letter;
    }

    private static boolean arabicLetters(String value) {
        if (value.isEmpty()) {
            return false;
        }
        for (int index = 0; index < value.length();) {
            int codePoint = value.codePointAt(index);
            index += Character.charCount(codePoint);
            if (!isArabicLetter(codePoint)) {
                return false;
            }
        }
        return true;
    }

    private static ValidationException invalid(String field, String message) {
        return new ValidationException(message, List.of(new FieldErrorDetail(field, message)));
    }
}
