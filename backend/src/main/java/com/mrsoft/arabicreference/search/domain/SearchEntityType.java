package com.mrsoft.arabicreference.search.domain;

public enum SearchEntityType {
    DICTIONARY_ENTRY(0),
    ROOT(1),
    GRAMMAR_CONCEPT(2),
    GRAMMAR_RULE(3),
    GRAMMAR_TOPIC(4),
    SPELLING_RULE(5),
    RHETORIC_DEVICE(6),
    LITERARY_FIGURE(7),
    LITERARY_WORK(8),
    ARTICLE(9),
    SPELLING_TOPIC(10),
    RHETORIC_TOPIC(11),
    LITERARY_ERA(12),
    LEARNING_PATH(13),
    LESSON(14);

    private final int priority;

    SearchEntityType(int priority) {
        this.priority = priority;
    }

    public int priority() {
        return priority;
    }

    public String suggestionKind() {
        return switch (this) {
            case DICTIONARY_ENTRY -> "word";
            case ROOT -> "root";
            case GRAMMAR_CONCEPT -> "grammar_concept";
            case GRAMMAR_TOPIC -> "grammar_topic";
            case GRAMMAR_RULE -> "grammar_rule";
            case SPELLING_RULE -> "spelling_rule";
            case SPELLING_TOPIC -> "spelling_topic";
            case RHETORIC_DEVICE -> "rhetoric_device";
            case RHETORIC_TOPIC -> "rhetoric_topic";
            case LITERARY_FIGURE -> "literary_figure";
            case LITERARY_WORK -> "literary_work";
            case LITERARY_ERA -> "literary_era";
            case ARTICLE -> "article";
            case LEARNING_PATH -> "learning_path";
            case LESSON -> "lesson";
        };
    }
}
