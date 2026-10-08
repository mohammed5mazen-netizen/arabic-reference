package com.mrsoft.arabicreference.learning.domain;

import java.util.List;

public final class QuestionPolicy {

    private QuestionPolicy() {
    }

    public static void check(QuestionType type, List<Boolean> correctFlags) {
        int options = correctFlags.size();
        int correct = 0;
        for (Boolean flag : correctFlags) {
            if (Boolean.TRUE.equals(flag)) {
                correct++;
            }
        }
        boolean valid = switch (type) {
            case MULTIPLE_CHOICE -> options >= 2 && correct == 1;
            case TRUE_FALSE -> options == 2 && correct == 1;
            case MULTIPLE_SELECT -> options >= 2 && correct >= 1;
        };
        if (!valid) {
            throw new IllegalArgumentException(type.name());
        }
    }
}
