package com.mrsoft.arabicreference.spelling.domain;

import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import java.util.List;
import java.util.UUID;

public final class SpellingExampleRules {

    private SpellingExampleRules() {
    }

    public static void check(
            SpellingExampleKind kind,
            String correctForm,
            String incorrectForm,
            String explanation,
            String contextNote,
            String commonForm,
            String reason,
            UUID citationId) {
        if (kind == null) {
            throw invalid("kind", "Choose an example kind.");
        }
        switch (kind) {
            case QUOTED -> {
                require(correctForm, "correctForm");
                require(explanation, "explanation");
                if (citationId == null) {
                    throw invalid("citationId", "A quoted spelling example needs a citation.");
                }
            }
            case CONTRAST -> {
                require(correctForm, "correctForm");
                require(explanation, "explanation");
                if (incorrectForm != null && !incorrectForm.isBlank() && (contextNote == null || contextNote.isBlank())) {
                    throw invalid("contextNote", "Say when the other form would still be acceptable.");
                }
            }
            case CONSTRUCTED -> {
                require(correctForm, "correctForm");
                require(explanation, "explanation");
            }
            case COMMON_MISTAKE -> {
                require(commonForm, "commonForm");
                require(correctForm, "correctForm");
                require(reason, "reason");
                if (citationId == null) {
                    throw invalid("citationId", "A common mistake needs a source.");
                }
            }
        }
    }

    private static void require(String value, String field) {
        if (value == null || value.isBlank()) {
            throw invalid(field, "This example needs " + field + ".");
        }
    }

    private static ValidationException invalid(String field, String message) {
        return new ValidationException(message, List.of(new FieldErrorDetail(field, message)));
    }
}
