package com.mrsoft.arabicreference.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.mrsoft.arabicreference.ai.application.QuestionClassifier;
import com.mrsoft.arabicreference.ai.domain.AssistantIntent;
import com.mrsoft.arabicreference.linguistics.domain.text.ArabicTextNormalizer;
import org.junit.jupiter.api.Test;

class QuestionClassifierTest {

    private static final ArabicTextNormalizer NORMALIZER = new ArabicTextNormalizer();

    @Test
    void routesMeaningRootGrammarSpellingRhetoricLiteratureAndComparison() {
        assertThat(classify("ما معنى كتاب؟").intent()).isEqualTo(AssistantIntent.WORD_MEANING);
        assertThat(classify("ما معنى كتاب؟").terms()).contains("كتاب");
        assertThat(classify("ما جذر كتاب؟").intent()).isEqualTo(AssistantIntent.ROOT);
        assertThat(classify("ما هو الفاعل؟").intent()).isEqualTo(AssistantIntent.GRAMMAR);
        assertThat(classify("هل كتابة إن صحيحة؟").intent()).isEqualTo(AssistantIntent.SPELLING);
        assertThat(classify("ما الاستعارة؟").intent()).isEqualTo(AssistantIntent.RHETORIC);
        assertThat(classify("من هو الشاعر؟").intent()).isEqualTo(AssistantIntent.LITERATURE);
        assertThat(classify("ما الفرق بين كتاب و كاتب؟").intent()).isEqualTo(AssistantIntent.COMPARISON);
        assertThat(classify("ما الفرق بين كتاب و كاتب؟").terms()).containsExactly("كتاب", "كاتب");
        assertThat(classify("أخبرني عن العربية").intent()).isEqualTo(AssistantIntent.GENERAL_LINGUISTIC);
    }

    @Test
    void marksSentenceParsingAsUnsupportedAlongsideAMeaningQuestion() {
        QuestionClassifier.Resolution resolution = classify("ما معنى كتاب وما إعراب هذه الجملة الطويلة");
        assertThat(resolution.intent()).isEqualTo(AssistantIntent.WORD_MEANING);
        assertThat(resolution.asksForParsing()).isTrue();
    }

    private static QuestionClassifier.Resolution classify(String question) {
        return QuestionClassifier.classify(NORMALIZER.normalize(question).normalizedText());
    }
}
