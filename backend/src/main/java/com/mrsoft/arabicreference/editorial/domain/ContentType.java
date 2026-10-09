package com.mrsoft.arabicreference.editorial.domain;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

public enum ContentType {
    DICTIONARY_ENTRY(true),
    MORPHOLOGY_ANALYSIS(false),
    GRAMMAR_TOPIC(true),
    GRAMMAR_RULE(true),
    GRAMMAR_CONCEPT(true),
    SPELLING_TOPIC(true),
    SPELLING_RULE(true),
    RHETORIC_TOPIC(true),
    RHETORIC_DEVICE(true),
    LITERARY_ERA(true),
    LITERARY_GENRE(false),
    LITERARY_SCHOOL(false),
    LITERARY_FIGURE(true),
    LITERARY_WORK(true),
    ARTICLE(true),
    LEARNING_PATH(true),
    SEARCH_INDEX(false);

    private final boolean searchable;

    ContentType(boolean searchable) {
        this.searchable = searchable;
    }

    public boolean searchable() {
        return searchable;
    }

    public String adminPath(String id) {
        return switch (this) {
            case DICTIONARY_ENTRY -> "/admin/dictionary/" + id;
            case GRAMMAR_RULE -> "/admin/grammar/rules/" + id;
            case GRAMMAR_TOPIC, GRAMMAR_CONCEPT -> "/admin/grammar";
            case MORPHOLOGY_ANALYSIS -> "/admin/morphology";
            case SPELLING_TOPIC, SPELLING_RULE -> "/admin/spelling";
            case RHETORIC_TOPIC, RHETORIC_DEVICE -> "/admin/rhetoric";
            case LITERARY_ERA, LITERARY_GENRE, LITERARY_SCHOOL, LITERARY_FIGURE, LITERARY_WORK -> "/admin/literature";
            case ARTICLE -> "/admin/articles";
            case LEARNING_PATH -> "/admin/learning";
            case SEARCH_INDEX -> "/admin/search";
        };
    }

    public static Optional<ContentType> parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return Optional.empty();
        }
        String normalized = raw.trim().toUpperCase(Locale.ROOT);
        return Arrays.stream(values()).filter(type -> type.name().equals(normalized)).findFirst();
    }
}
