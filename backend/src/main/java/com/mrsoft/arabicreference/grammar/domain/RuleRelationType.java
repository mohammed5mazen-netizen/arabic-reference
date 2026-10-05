package com.mrsoft.arabicreference.grammar.domain;

public enum RuleRelationType {
    RELATED_TO("ذات صلة"),
    PREREQUISITE_OF("تمهيد ل"),
    EXCEPTION_TO("استثناء من"),
    SPECIAL_CASE_OF("حالة خاصة من"),
    CONTRASTS_WITH("يقابل"),
    SEE_ALSO("انظر أيضًا");

    private final String arabicLabel;

    RuleRelationType(String arabicLabel) {
        this.arabicLabel = arabicLabel;
    }

    public String arabicLabel() {
        return arabicLabel;
    }
}
