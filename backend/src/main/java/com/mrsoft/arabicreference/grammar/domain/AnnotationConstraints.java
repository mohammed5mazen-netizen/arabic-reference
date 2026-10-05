package com.mrsoft.arabicreference.grammar.domain;

import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class AnnotationConstraints {

    private AnnotationConstraints() {
    }

    public static void assertCompatible(StateKind kind, GrammaticalState state) {
        if (state == null || kind == null || kind == StateKind.UNSPECIFIED) {
            return;
        }
        boolean allowed = switch (kind) {
            case NOMINAL_CASE -> state != GrammaticalState.JAZM;
            case VERBAL_MOOD -> state != GrammaticalState.JARR;
            case UNSPECIFIED -> true;
        };
        if (!allowed) {
            throw invalid("grammaticalState", "This case or mood does not apply to the selected grammatical role.");
        }
    }

    public static void assertPositions(List<Integer> positions) {
        Set<Integer> seen = new HashSet<>();
        for (Integer position : positions) {
            if (position == null || position < 0) {
                throw invalid("position", "Token positions start at zero.");
            }
            if (!seen.add(position)) {
                throw invalid("position", "Each token position must be unique.");
            }
        }
    }

    private static ValidationException invalid(String field, String message) {
        return new ValidationException(message, List.of(new FieldErrorDetail(field, message)));
    }
}
