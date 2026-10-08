package com.mrsoft.arabicreference.ai.domain;

public final class EvidenceScores {

    public static final int EXACT = 100;
    public static final int VERIFIED = 90;
    public static final int DOMAIN = 80;
    public static final int SEARCH = 60;
    public static final int RULE = 40;
    public static final int FUZZY = 10;
    public static final int USABLE = RULE;

    private EvidenceScores() {
    }

    public static int capLearning(boolean lessonQuestion, int score) {
        return Math.min(score, lessonQuestion ? DOMAIN : SEARCH);
    }
}
