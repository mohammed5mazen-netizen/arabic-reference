package com.mrsoft.arabicreference.search.domain;

import com.mrsoft.arabicreference.linguistics.domain.text.ArabicSearchNormalizer;
import com.mrsoft.arabicreference.linguistics.domain.text.ArabicTextNormalizer;

public final class SearchText {

    private static final ArabicTextNormalizer NORMALIZER = new ArabicTextNormalizer();

    private SearchText() {
    }

    public static String normalized(String original) {
        return NORMALIZER.normalize(original == null ? "" : original).normalizedText();
    }

    public static String key(String original) {
        return ArabicSearchNormalizer.key(original == null ? "" : original);
    }

    public static String blob(String... parts) {
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part == null || part.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(part.trim());
        }
        return SearchSnippets.truncate(ArabicSearchNormalizer.folded(builder.toString()), 4000);
    }

    public static String snippet(String... parts) {
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part == null || part.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(part.trim());
        }
        return SearchSnippets.truncate(builder.toString(), 800);
    }
}
