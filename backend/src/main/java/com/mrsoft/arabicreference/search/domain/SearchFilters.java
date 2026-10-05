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
        GRAMMAR
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
            default -> throw invalid("type", "Choose dictionary, root, or grammar.");
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
        };
    }

    private static ValidationException invalid(String field, String message) {
        return new ValidationException(message, List.of(new FieldErrorDetail(field, message)));
    }
}
