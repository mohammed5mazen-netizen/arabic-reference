package com.mrsoft.arabicreference.spelling.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SpellingExampleRulesTest {

    @Test
    void quotedAndCommonMistakesNeedACitation() {
        UUID citation = UUID.randomUUID();
        assertThatThrownBy(() -> SpellingExampleRules.check(SpellingExampleKind.QUOTED, "إن", null, "شرح", null, null, null, null))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> SpellingExampleRules.check(SpellingExampleKind.COMMON_MISTAKE, "إن", null, null, null, "ان", "السبب", null))
                .isInstanceOf(ValidationException.class);
        assertThatCode(() -> SpellingExampleRules.check(SpellingExampleKind.QUOTED, "إن", null, "شرح", null, null, null, citation))
                .doesNotThrowAnyException();
        assertThatCode(() -> SpellingExampleRules.check(SpellingExampleKind.COMMON_MISTAKE, "إن", null, null, null, "ان", "السبب", citation))
                .doesNotThrowAnyException();
    }

    @Test
    void contrastKeepsTheOtherFormContextual() {
        assertThatThrownBy(() -> SpellingExampleRules.check(SpellingExampleKind.CONTRAST, "إنّ", "ان", "شرح", null, null, null, null))
                .isInstanceOf(ValidationException.class);
        assertThatCode(() -> SpellingExampleRules.check(SpellingExampleKind.CONTRAST, "إنّ", "ان", "شرح", "يصح في سياق آخر", null, null, null))
                .doesNotThrowAnyException();
        assertThatCode(() -> SpellingExampleRules.check(SpellingExampleKind.CONSTRUCTED, "همزة", null, "مثال تحريري", null, null, null, null))
                .doesNotThrowAnyException();
    }
}
