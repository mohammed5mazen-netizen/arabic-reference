package com.mrsoft.arabicreference.search.domain;

import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class SearchFilters {

    public enum TypeGroup {
        ALL,
        DICTIONARY,
        ROOT,
        GRAMMAR,
        SPELLING,
        RHETORIC,
        LITERATURE,
        ARTICLES,
        CONTENT
    }

    private static final Set<String> PARTS_OF_SPEECH = Set.of(
            "NOUN",
            "VERB",
            "ADJECTIVE",
            "ADVERB",
            "PRONOUN",
            "PREPOSITION",
            "CONJUNCTION",
            "PARTICLE",
            "INTERJECTION",
            "PROPER_NOUN",
            "OTHER");

    private SearchFilters() {
    }

    public static TypeGroup type(String raw) {
        if (raw == null || raw.isBlank() || raw.equalsIgnoreCase("all")) {
            return TypeGroup.ALL;
        }
        return switch (raw.toLowerCase(Locale.ROOT)) {
            case "dictionary" -> TypeGroup.DICTIONARY;
            case "root" -> TypeGroup.ROOT;
            case "grammar" -> TypeGroup.GRAMMAR;
            case "spelling" -> TypeGroup.SPELLING;
            case "rhetoric" -> TypeGroup.RHETORIC;
            case "literature" -> TypeGroup.LITERATURE;
            case "articles" -> TypeGroup.ARTICLES;
            case "content" -> TypeGroup.CONTENT;
            default -> throw invalid("type", "Choose dictionary, root, grammar, spelling, rhetoric, literature, articles, or content.");
        };
    }

    public static String partOfSpeech(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String code = raw.trim().toUpperCase(Locale.ROOT);
        if (!PARTS_OF_SPEECH.contains(code)) {
            throw invalid("partOfSpeech", "That part of speech is not supported.");
        }
        return code;
    }

    public static boolean accepts(TypeGroup group, SearchEntityType type) {
        return switch (group) {
            case ALL -> true;
            case DICTIONARY -> type == SearchEntityType.DICTIONARY_ENTRY;
            case ROOT -> type == SearchEntityType.ROOT;
            case GRAMMAR -> type == SearchEntityType.GRAMMAR_TOPIC
                    || type == SearchEntityType.GRAMMAR_RULE
                    || type == SearchEntityType.GRAMMAR_CONCEPT;
            case SPELLING -> type == SearchEntityType.SPELLING_RULE || type == SearchEntityType.SPELLING_TOPIC;
            case RHETORIC -> type == SearchEntityType.RHETORIC_DEVICE || type == SearchEntityType.RHETORIC_TOPIC;
            case LITERATURE -> type == SearchEntityType.LITERARY_FIGURE
                    || type == SearchEntityType.LITERARY_WORK
                    || type == SearchEntityType.LITERARY_ERA;
            case ARTICLES -> type == SearchEntityType.ARTICLE;
            case CONTENT -> accepts(TypeGroup.SPELLING, type)
                    || accepts(TypeGroup.RHETORIC, type)
                    || accepts(TypeGroup.LITERATURE, type)
                    || accepts(TypeGroup.ARTICLES, type);
        };
    }

    private static ValidationException invalid(String field, String message) {
        return new ValidationException(message, List.of(new FieldErrorDetail(field, message)));
    }
}
