package com.mrsoft.arabicreference.grammar.domain;

/**
 * Editorial learning metadata. It is not an academic verdict.
 */
public enum DifficultyLevel {
    BEGINNER("مبتدئ"),
    INTERMEDIATE("متوسط"),
    ADVANCED("متقدم");

    private final String arabicLabel;

    DifficultyLevel(String arabicLabel) {
        this.arabicLabel = arabicLabel;
    }

    public String arabicLabel() {
        return arabicLabel;
    }
}
