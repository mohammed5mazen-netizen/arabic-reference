package com.mrsoft.arabicreference.learning.domain;

public enum ActivityType {
    READ,
    MULTIPLE_CHOICE,
    TRUE_FALSE,
    MATCHING,
    CLASSIFICATION;

    public boolean scoredInQuiz() {
        return this == MULTIPLE_CHOICE || this == TRUE_FALSE;
    }

    public String label() {
        return switch (this) {
            case READ -> "قراءة";
            case MULTIPLE_CHOICE -> "اختيار من متعدد";
            case TRUE_FALSE -> "صواب أو خطأ";
            case MATCHING -> "مطابقة";
            case CLASSIFICATION -> "تصنيف";
        };
    }
}
