package com.mrsoft.arabicreference.grammar.domain;

/**
 * Structured parts of a rule. Conditions and exceptions are component types, not a second model.
 */
public enum ComponentType {
    DEFINITION("التعريف"),
    CORE_RULE("القاعدة"),
    CONDITION("الشرط"),
    EXCEPTION("الاستثناء"),
    NOTE("ملاحظة"),
    WARNING("تنبيه"),
    TERMINOLOGY("مصطلح"),
    DIFFERENCE("فرق"),
    SCHOLARLY_NOTE("فائدة");

    private final String arabicLabel;

    ComponentType(String arabicLabel) {
        this.arabicLabel = arabicLabel;
    }

    public String arabicLabel() {
        return arabicLabel;
    }
}
