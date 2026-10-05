package com.mrsoft.arabicreference.search.domain;

import com.mrsoft.arabicreference.linguistics.domain.text.ArabicSearchNormalizer;
import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import java.util.List;

public final class SearchQueryPolicy {

    private SearchQueryPolicy() {
    }

    public record PreparedQuery(String display, String key, String folded) {
    }

    public static PreparedQuery prepare(String query) {
        if (query == null || query.isBlank()) {
            throw invalid("Enter a search query.");
        }
        String display = query.trim();
        int points = display.codePointCount(0, display.length());
        if (points < SearchTuning.MIN_CODE_POINTS) {
            throw invalid("Enter at least two characters.");
        }
        if (points > SearchTuning.MAX_CODE_POINTS) {
            throw invalid("The search query is too long.");
        }
        String key = ArabicSearchNormalizer.key(display);
        String folded = ArabicSearchNormalizer.folded(display);
        if (key.isBlank() || key.codePointCount(0, key.length()) < 1) {
            throw invalid("Enter a search query.");
        }
        return new PreparedQuery(display, key, folded);
    }

    public static int page(int page) {
        if (page < 1) {
            throw invalid("page", "Page numbering starts at 1.");
        }
        return page;
    }

    public static int size(int size) {
        if (size < 1 || size > SearchTuning.MAX_PAGE_SIZE) {
            throw invalid("size", "Page size must be from 1 to " + SearchTuning.MAX_PAGE_SIZE + ".");
        }
        return size;
    }

    private static ValidationException invalid(String message) {
        return invalid("q", message);
    }

    private static ValidationException invalid(String field, String message) {
        return new ValidationException(message, List.of(new FieldErrorDetail(field, message)));
    }
}
