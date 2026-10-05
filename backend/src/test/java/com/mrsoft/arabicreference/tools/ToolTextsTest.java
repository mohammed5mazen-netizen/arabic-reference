package com.mrsoft.arabicreference.tools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import com.mrsoft.arabicreference.tools.application.ToolCacheKey;
import com.mrsoft.arabicreference.tools.application.ToolTexts;
import com.mrsoft.arabicreference.tools.domain.ToolCode;
import org.junit.jupiter.api.Test;

class ToolTextsTest {

    @Test
    void oneArabicWordIsAcceptedAndLongTextIsRejected() {
        assertThat(ToolTexts.word(ToolCode.ROOT, " كتاب ").normalized()).isEqualTo("كتاب");
        assertThatThrownBy(() -> ToolTexts.word(ToolCode.WORD_ANALYSIS, "كتاب كبير")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> ToolTexts.phrase(ToolCode.SPELLING_CHECK, "نص ".repeat(40))).isInstanceOf(ValidationException.class)
                .hasMessageContaining("أطول");
        assertThatThrownBy(() -> ToolTexts.word(ToolCode.ROOT, "<script>alert(1)</script>")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> ToolTexts.word(ToolCode.ROOT, "' or 1=1")).isInstanceOf(ValidationException.class);
    }

    @Test
    void cacheKeyChangesWhenPublishedGenerationChanges() {
        String first = ToolCacheKey.of("ROOT", "كتاب", "stamp-a", 1, 2, 3);
        String second = ToolCacheKey.of("ROOT", "كتاب", "stamp-b", 1, 2, 3);
        String ruled = ToolCacheKey.of("ROOT", "كتاب", "stamp-a", 4, 2, 3);
        assertThat(first).isNotEqualTo(second);
        assertThat(first).isNotEqualTo(ruled);
    }
}
