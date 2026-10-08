package com.mrsoft.arabicreference.ai.domain;

public enum GroundingStatus {
    GROUNDED,
    PARTIALLY_GROUNDED,
    INSUFFICIENT_EVIDENCE;

    public String label() {
        return switch (this) {
            case GROUNDED -> "موثّق من المرجع";
            case PARTIALLY_GROUNDED -> "إجابة جزئية من المرجع";
            case INSUFFICIENT_EVIDENCE -> "لا تتوفر معلومات كافية";
        };
    }

    public boolean cited() {
        return this != INSUFFICIENT_EVIDENCE;
    }
}
