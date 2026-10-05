package com.mrsoft.arabicreference.rhetoric.domain;

import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import java.util.List;
import java.util.UUID;

public final class RhetoricExampleRules {

    private RhetoricExampleRules() {
    }

    public static void check(RhetoricExampleKind kind, String text, String explanation, UUID citationId) {
        if (kind == null) {
            throw invalid("kind", "Choose whether the example is quoted or constructed.");
        }
        if (text == null || text.isBlank()) {
            throw invalid("text", "Enter the example text.");
        }
        if (explanation == null || explanation.isBlank()) {
            throw invalid("explanation", "Explain the rhetorical reading.");
        }
        if (kind == RhetoricExampleKind.QUOTED && citationId == null) {
            throw invalid("citationId", "A quoted rhetorical example needs a citation.");
        }
    }

    private static ValidationException invalid(String field, String message) {
        return new ValidationException(message, List.of(new FieldErrorDetail(field, message)));
    }
}
