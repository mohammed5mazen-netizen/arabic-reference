package com.mrsoft.arabicreference.learning.domain;

/**
 * Derived on the visitor's device. Opening a page is not completion.
 */
public enum ProgressState {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED;

    public String label() {
        return switch (this) {
            case NOT_STARTED -> "لم يبدأ";
            case IN_PROGRESS -> "قيد التعلم";
            case COMPLETED -> "مكتمل";
        };
    }

    public static ProgressState of(boolean started, boolean completed) {
        if (completed) {
            return COMPLETED;
        }
        return started ? IN_PROGRESS : NOT_STARTED;
    }
}
