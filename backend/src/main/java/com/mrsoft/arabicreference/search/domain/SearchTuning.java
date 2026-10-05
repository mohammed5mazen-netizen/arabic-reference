package com.mrsoft.arabicreference.search.domain;

public final class SearchTuning {

    public static final int INDEX_VERSION = 2;
    public static final int MIN_CODE_POINTS = 2;
    public static final int MAX_CODE_POINTS = 120;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 50;
    public static final int CANDIDATE_CAP = 200;
    public static final int FUZZY_MIN_CODE_POINTS = 4;
    public static final double FUZZY_THRESHOLD = 0.45;
    public static final int FUZZY_CAP = 5;
    public static final int SOLID_RESULTS_BEFORE_FUZZY = 3;
    public static final int MORPHOLOGY_CAP = 5;
    public static final int SUGGESTION_CAP = 8;
    public static final int SNIPPET_CODE_POINTS = 160;
    public static final int ROOT_RELATED_CAP = 3;
    public static final long ADVISORY_LOCK_KEY = 54051L;

    private SearchTuning() {
    }
}
