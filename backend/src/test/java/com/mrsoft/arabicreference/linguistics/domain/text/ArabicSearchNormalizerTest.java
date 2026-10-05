package com.mrsoft.arabicreference.linguistics.domain.text;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ArabicSearchNormalizerTest {

    private final ArabicTextNormalizer base = new ArabicTextNormalizer();

    @Test
    void searchKeyKeepsBaseFoldingAndAddsLookupRules() {
        assertThat(ArabicSearchNormalizer.key("كِتَاب")).isEqualTo("كتاب");
        assertThat(ArabicSearchNormalizer.key("كتــــاب")).isEqualTo(base.normalize("كتــــاب").normalizedText());
        assertThat(ArabicSearchNormalizer.key("أإآ")).isEqualTo("ااا");
        assertThat(ArabicSearchNormalizer.key("ؤئءةى")).isEqualTo("ؤئءةى");
        assertThat(ArabicSearchNormalizer.key("الكتاب")).isEqualTo("كتاب");
        assertThat(ArabicSearchNormalizer.key("الله")).isEqualTo("الله");
        assertThat(ArabicSearchNormalizer.key("كتاب،")).isEqualTo("كتاب");
        assertThat(ArabicSearchNormalizer.key("١٢٣")).isEqualTo("123");
        assertThat(ArabicSearchNormalizer.key("kitab")).isEqualTo("kitab");
        assertThat(ArabicSearchNormalizer.folded("الشخص الذي يكتب")).isEqualTo("الشخص الذي يكتب");
        assertThat(ArabicSearchNormalizer.key("  كتاب  ")).isEqualTo("كتاب");
    }

    @Test
    void nullIsRejected() {
        assertThatThrownBy(() -> ArabicSearchNormalizer.key(null)).isInstanceOf(IllegalArgumentException.class);
    }
}
