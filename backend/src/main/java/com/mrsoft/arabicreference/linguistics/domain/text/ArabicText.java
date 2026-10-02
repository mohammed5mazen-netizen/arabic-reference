package com.mrsoft.arabicreference.linguistics.domain.text;

import java.util.Objects;

/**
 * Original Arabic text is never replaced by its normalized form.
 */
public record ArabicText(String originalText, String normalizedText) {

    public ArabicText {
        Objects.requireNonNull(originalText, "originalText");
        Objects.requireNonNull(normalizedText, "normalizedText");
    }
}
