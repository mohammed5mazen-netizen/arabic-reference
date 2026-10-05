package com.mrsoft.arabicreference.literature.domain;

import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import java.util.List;
import java.util.UUID;

public final class ExcerptRules {

    public static final int MAX_CODE_POINTS = 800;

    private ExcerptRules() {
    }

    public static void check(WorkRights rights, String text, UUID citationId) {
        if (rights == null || !rights.allowsExcerpt()) {
            throw invalid("rights", "An excerpt is allowed only when the work is public domain or licensed.");
        }
        if (citationId == null) {
            throw invalid("citationId", "An excerpt needs a citation.");
        }
        int count = text == null ? 0 : text.trim().codePointCount(0, text.trim().length());
        if (count < 1 || count > MAX_CODE_POINTS) {
            throw invalid("text", "Keep the excerpt within 800 characters.");
        }
    }

    private static ValidationException invalid(String field, String message) {
        return new ValidationException(message, List.of(new FieldErrorDetail(field, message)));
    }
}
