package com.mrsoft.arabicreference.learning.domain;

public enum SectionType {
    INTRODUCTION,
    EXPLANATION,
    EXAMPLE,
    NOTE,
    WARNING,
    SUMMARY,
    REFERENCE,
    ACTIVITY;

    public String label() {
        return switch (this) {
            case INTRODUCTION -> "تمهيد";
            case EXPLANATION -> "شرح";
            case EXAMPLE -> "مثال";
            case NOTE -> "ملاحظة";
            case WARNING -> "تنبيه";
            case SUMMARY -> "خلاصة";
            case REFERENCE -> "مرجع";
            case ACTIVITY -> "نشاط";
        };
    }
}
