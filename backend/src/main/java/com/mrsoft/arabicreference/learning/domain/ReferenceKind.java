package com.mrsoft.arabicreference.learning.domain;

public enum ReferenceKind {
    DICTIONARY_ENTRY,
    GRAMMAR_RULE,
    GRAMMAR_CONCEPT,
    GRAMMAR_TOPIC,
    SPELLING_RULE,
    RHETORIC_DEVICE,
    ARTICLE,
    MORPHOLOGY_TOOL;

    public String label() {
        return switch (this) {
            case DICTIONARY_ENTRY -> "مدخل معجمي";
            case GRAMMAR_RULE -> "قاعدة نحوية";
            case GRAMMAR_CONCEPT -> "مصطلح نحوي";
            case GRAMMAR_TOPIC -> "موضوع نحوي";
            case SPELLING_RULE -> "قاعدة إملائية";
            case RHETORIC_DEVICE -> "فن بلاغي";
            case ARTICLE -> "مقالة";
            case MORPHOLOGY_TOOL -> "أداة صرفية";
        };
    }
}
