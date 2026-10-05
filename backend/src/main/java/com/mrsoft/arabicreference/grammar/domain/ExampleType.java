package com.mrsoft.arabicreference.grammar.domain;

public enum ExampleType {
    CONSTRUCTED("مثال تحريري", false),
    QUOTED("مثال مقتبس", true),
    QURANIC("شاهد قرآني", true),
    POETRY("شاهد شعري", true),
    PROSE("شاهد نثري", true),
    COUNTEREXAMPLE("مثال مقابل", false),
    OTHER("مثال آخر", false);

    private final String arabicLabel;
    private final boolean citationRequired;

    ExampleType(String arabicLabel, boolean citationRequired) {
        this.arabicLabel = arabicLabel;
        this.citationRequired = citationRequired;
    }

    public String arabicLabel() {
        return arabicLabel;
    }

    public boolean citationRequired() {
        return citationRequired;
    }

    public boolean editorial() {
        return !citationRequired;
    }
}
