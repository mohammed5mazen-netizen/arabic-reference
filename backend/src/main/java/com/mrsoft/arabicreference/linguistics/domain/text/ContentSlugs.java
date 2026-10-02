package com.mrsoft.arabicreference.linguistics.domain.text;

import java.util.UUID;

/**
 * Stable public slug: normalized label plus the first 8 hex characters of the id.
 * A lemma alone is not an identity, so the suffix separates two entries of the same word.
 */
public final class ContentSlugs {

    private ContentSlugs() {
    }

    public static String of(String normalizedLabel, UUID id) {
        String label = normalizedLabel == null ? "" : normalizedLabel.trim().replace(' ', '-');
        if (label.isEmpty()) {
            label = "item";
        }
        return label + "-" + id.toString().replace("-", "").substring(0, 8);
    }
}
