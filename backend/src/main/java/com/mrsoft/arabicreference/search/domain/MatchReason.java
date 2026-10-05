package com.mrsoft.arabicreference.search.domain;

/**
 * Deterministic ranks. Higher scores win. The numeric score is an ordering key, not a user-facing value.
 * Tie break after score is entity-type priority, normalized title, then id.
 */
public enum MatchReason {
    EXACT(900),
    NORMALIZED_EXACT(800),
    WORD_FORM(700),
    ALIAS(690),
    ROOT(600),
    TITLE_PREFIX(500),
    MORPHOLOGY(400),
    DEFINITION(300),
    FUZZY(200);

    public static final int FUZZY_BODY_SCORE = 100;
    public static final int ROOT_RELATED_SCORE = 580;

    private final int score;

    MatchReason(int score) {
        this.score = score;
    }

    public int score() {
        return score;
    }
}
