package com.mrsoft.arabicreference.linguistics.domain.text;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ArabicTextNormalizerTest {

    private final ArabicTextNormalizer normalizer = new ArabicTextNormalizer();

    @Test
    void keepsOriginalTextBesideNormalizedText() {
        String original = "كِتَابٌ";

        ArabicText text = normalizer.normalize(original);

        assertThat(text.originalText()).isEqualTo(original);
        assertThat(text.normalizedText()).isEqualTo("كتاب");
    }

    @Test
    void removesTatweelFromNormalizedTextOnly() {
        String original = "كتـــاب";

        ArabicText text = normalizer.normalize(original);

        assertThat(text.originalText()).isEqualTo(original);
        assertThat(text.normalizedText()).isEqualTo("كتاب");
    }

    @Test
    void foldsAlefVariantsWithoutChangingTheOriginal() {
        ArabicText text = normalizer.normalize("أحمد وإبراهيم وآية وٱسم");

        assertThat(text.originalText()).contains("أ").contains("إ").contains("آ").contains("ٱ");
        assertThat(text.normalizedText()).isEqualTo("احمد وابراهيم واية واسم");
    }

    @Test
    void preservesTaaMarbutaAndAlefMaksura() {
        ArabicText text = normalizer.normalize("مدرسة على");

        assertThat(text.normalizedText()).isEqualTo("مدرسة على");
    }

    @Test
    void preservesStandaloneHamzaForms() {
        ArabicText text = normalizer.normalize("سؤال هيئة شيء");

        assertThat(text.normalizedText()).isEqualTo("سؤال هيئة شيء");
    }

    @Test
    void collapsesWhitespaceAndUnusualSpaces() {
        ArabicText text = normalizer.normalize("  كتاب\u00A0\u00A0النحو\n\t ");

        assertThat(text.normalizedText()).isEqualTo("كتاب النحو");
    }

    @Test
    void removesBidirectionalMarksFromNormalizedText() {
        ArabicText text = normalizer.normalize("كتاب\u200F");

        assertThat(text.originalText()).contains("\u200F");
        assertThat(text.normalizedText()).isEqualTo("كتاب");
    }

    @Test
    void appliesUnicodeNfc() {
        ArabicText text = normalizer.normalize("cafe\u0301");

        assertThat(text.originalText()).isEqualTo("cafe\u0301");
        assertThat(text.normalizedText()).isEqualTo("caf\u00e9");
    }

    @Test
    void normalizationIsIdempotent() {
        ArabicText once = normalizer.normalize("  أَحْمَدُ\u0640 ");
        ArabicText twice = normalizer.normalize(once.normalizedText());

        assertThat(twice.normalizedText()).isEqualTo(once.normalizedText());
    }

    @Test
    void emptyTextStaysEmpty() {
        assertThat(normalizer.normalize("").normalizedText()).isEmpty();
    }

    @Test
    void rejectsNull() {
        assertThatThrownBy(() -> normalizer.normalize(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("originalText must not be null");
    }
}
