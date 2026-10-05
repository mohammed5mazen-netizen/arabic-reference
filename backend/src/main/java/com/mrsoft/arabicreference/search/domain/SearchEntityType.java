package com.mrsoft.arabicreference.search.domain;

public enum SearchEntityType {
    DICTIONARY_ENTRY(0),
    ROOT(1),
    GRAMMAR_CONCEPT(2),
    GRAMMAR_RULE(3),
    GRAMMAR_TOPIC(4);

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
        };
    }
}
