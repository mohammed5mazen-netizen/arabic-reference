package com.mrsoft.arabicreference.grammar.domain;

/**
 * Case and mood labels owned by the grammar module.
 * They are not imported from the morphology engine.
 */
public enum GrammaticalState {
    RAFA("مرفوع"),
    NASB("منصوب"),
    JARR("مجرور"),
    JAZM("مجزوم");

    private final String arabicLabel;

    GrammaticalState(String arabicLabel) {
        this.arabicLabel = arabicLabel;
    }

    public String arabicLabel() {
        return arabicLabel;
    }
}
