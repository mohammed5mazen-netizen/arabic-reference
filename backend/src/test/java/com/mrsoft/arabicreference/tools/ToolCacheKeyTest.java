package com.mrsoft.arabicreference.tools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import com.mrsoft.arabicreference.tools.application.ToolCacheKey;
import com.mrsoft.arabicreference.tools.application.ToolTexts;
import com.mrsoft.arabicreference.tools.domain.ToolCode;
import org.junit.jupiter.api.Test;

class ToolCacheKeyTest {

    @Test
    void cacheKeyChangesWhenPublishedDataChanges() {
        String first = ToolCacheKey.of("ROOT", "كتاب", "stamp-a", 1, 2, 2);
        String second = ToolCacheKey.of("ROOT", "كتاب", "stamp-b", 1, 2, 2);
        assertThat(first).isNotEqualTo(second);
        assertThat(ToolCacheKey.of("ROOT", "كتاب", "stamp-a", 2, 2, 2)).isNotEqualTo(first);
    }

    @Test
    void longTextAndHostileInputAreRejected() {
        assertThatThrownBy(() -> ToolTexts.word(ToolCode.WORD_ANALYSIS, "كتاب كتاب")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> ToolTexts.phrase(ToolCode.SPELLING_CHECK, "<script>alert(1)</script>")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> ToolTexts.word(ToolCode.ROOT, "كتاب".repeat(30))).isInstanceOf(ValidationException.class);
        assertThat(ToolTexts.word(ToolCode.ROOT, " كتاب ").normalized()).isEqualTo("كتاب");
    }
}
