package com.mrsoft.arabicreference.morphology.domain;

import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import java.util.List;

public final class PatternCompatibility {

    private PatternCompatibility() {
    }

    public static boolean compatible(int patternRadicals, Integer rootRadicals) {
        return rootRadicals != null && patternRadicals == rootRadicals;
    }

    public static void require(int patternRadicals, Integer rootRadicals) {
        if (!compatible(patternRadicals, rootRadicals)) {
            throw new ValidationException(
                    "This pattern does not match the root length.",
                    List.of(new FieldErrorDetail("pattern", "A triliteral pattern cannot be applied to a different root length.")));
        }
    }
}
