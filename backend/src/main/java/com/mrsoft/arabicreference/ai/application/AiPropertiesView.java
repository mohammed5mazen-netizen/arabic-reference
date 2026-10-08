package com.mrsoft.arabicreference.ai.application;

public interface AiPropertiesView {

    boolean operational();

    boolean cacheEnabled();

    boolean keyConfigured();

    String provider();

    String model();

    int maxQuestionCodePoints();

    int maxQuestionLines();

    int maxContextItems();

    int maxExcerptChars();

    int maxOutputTokens();

    int totalBudget();
}
