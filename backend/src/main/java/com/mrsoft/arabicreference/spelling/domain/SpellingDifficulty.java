package com.mrsoft.arabicreference.spelling.domain;

public enum SpellingDifficulty {
    BEGINNER("مبتدئ"),
    INTERMEDIATE("متوسط"),
    ADVANCED("متقدم");

    private final String arabicLabel;

    SpellingDifficulty(String arabicLabel) {
        this.arabicLabel = arabicLabel;
    }

    public String arabicLabel() {
        return arabicLabel;
    }
}
