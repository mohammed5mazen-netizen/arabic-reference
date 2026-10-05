package com.mrsoft.arabicreference.rhetoric.domain;

public enum RhetoricCategory {
    MAANI("علم المعاني"),
    BAYAN("علم البيان"),
    BADI("علم البديع"),
    OTHER("قسم آخر");

    private final String arabicLabel;

    RhetoricCategory(String arabicLabel) {
        this.arabicLabel = arabicLabel;
    }

    public String arabicLabel() {
        return arabicLabel;
    }
}
