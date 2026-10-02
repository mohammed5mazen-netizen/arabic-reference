package com.mrsoft.arabicreference.dictionary.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import org.junit.jupiter.api.Test;

class ArabicLexicalTextTest {

    @Test
    void rootsAcceptTriliteralAndQuadriliteralAndNotedExceptions() {
        assertThat(ArabicLexicalText.requireRoot("كتب", null)).isEqualTo("كتب");
        assertThat(ArabicLexicalText.requireRoot("دحرج", null)).isEqualTo("دحرج");
        assertThat(ArabicLexicalText.requireRoot("قل", "ثنائي نادر")).isEqualTo("قل");
        assertThatThrownBy(() -> ArabicLexicalText.requireRoot("قل", " ")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> ArabicLexicalText.requireRoot("book", null)).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> ArabicLexicalText.requireRoot("ك", null)).isInstanceOf(ValidationException.class);
    }

    @Test
    void lemmasStayArabic() {
        assertThat(ArabicLexicalText.requireLemma("كتاب")).isEqualTo("كتاب");
        assertThatThrownBy(() -> ArabicLexicalText.requireLemma("book")).isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> ArabicLexicalText.requireLemma("")).isInstanceOf(ValidationException.class);
    }
}
