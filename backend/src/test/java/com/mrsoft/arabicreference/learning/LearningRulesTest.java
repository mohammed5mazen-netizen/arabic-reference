package com.mrsoft.arabicreference.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mrsoft.arabicreference.learning.domain.ActivityType;
import com.mrsoft.arabicreference.learning.domain.Difficulty;
import com.mrsoft.arabicreference.learning.domain.DisplayOrder;
import com.mrsoft.arabicreference.learning.domain.PathProgress;
import com.mrsoft.arabicreference.learning.domain.ProgressState;
import com.mrsoft.arabicreference.learning.domain.QuestionPolicy;
import com.mrsoft.arabicreference.learning.domain.QuestionType;
import com.mrsoft.arabicreference.learning.domain.QuizScorer;
import com.mrsoft.arabicreference.learning.domain.SectionType;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class LearningRulesTest {

    @Test
    void difficultyAndSectionsUseArabicLabels() {
        assertThat(Difficulty.BEGINNER.label()).isEqualTo("مبتدئ");
        assertThat(Difficulty.INTERMEDIATE.label()).isEqualTo("متوسط");
        assertThat(Difficulty.ADVANCED.label()).isEqualTo("متقدم");
        assertThat(SectionType.EXAMPLE.label()).isEqualTo("مثال");
        assertThat(ActivityType.READ.label()).isEqualTo("قراءة");
        assertThat(ProgressState.of(false, false)).isEqualTo(ProgressState.NOT_STARTED);
        assertThat(ProgressState.of(true, false).label()).isEqualTo("قيد التعلم");
        assertThat(ProgressState.of(true, true)).isEqualTo(ProgressState.COMPLETED);
    }

    @Test
    void ordersAreContiguousAndProgressIsDerived() {
        DisplayOrder.requireContiguous(List.of(2, 1, 3));
        assertThatThrownBy(() -> DisplayOrder.requireContiguous(List.of(1, 3))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> DisplayOrder.requireContiguous(List.of(1, 1))).isInstanceOf(IllegalArgumentException.class);
        assertThat(PathProgress.percent(1, 4)).isEqualTo(25);
        assertThat(PathProgress.percent(0, 4)).isZero();
        assertThat(PathProgress.percent(5, 4)).isEqualTo(100);
    }

    @Test
    void questionsRequireALegalCorrectSet() {
        QuestionPolicy.check(QuestionType.MULTIPLE_CHOICE, List.of(true, false));
        QuestionPolicy.check(QuestionType.TRUE_FALSE, List.of(false, true));
        QuestionPolicy.check(QuestionType.MULTIPLE_SELECT, List.of(true, true, false));
        assertThatThrownBy(() -> QuestionPolicy.check(QuestionType.MULTIPLE_CHOICE, List.of(true))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> QuestionPolicy.check(QuestionType.MULTIPLE_CHOICE, List.of(true, true))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> QuestionPolicy.check(QuestionType.TRUE_FALSE, List.of(true))).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> QuestionPolicy.check(QuestionType.MULTIPLE_SELECT, List.of(false, false))).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void scoringIsAllOrNothingAndDeterministic() {
        UUID correct = UUID.randomUUID();
        UUID extra = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        QuizScorer.Key choice = new QuizScorer.Key(UUID.randomUUID(), QuestionType.MULTIPLE_CHOICE, Set.of(correct));
        QuizScorer.Key multi = new QuizScorer.Key(UUID.randomUUID(), QuestionType.MULTIPLE_SELECT, Set.of(correct, extra));
        assertThat(QuizScorer.matches(choice, List.of(correct))).isTrue();
        assertThat(QuizScorer.matches(multi, List.of(correct))).isFalse();
        assertThat(QuizScorer.matches(multi, List.of(extra, correct))).isTrue();
        QuizScorer.Outcome outcome = QuizScorer.score(70, List.of(choice, multi), List.of(List.of(correct), List.of(correct, extra)));
        assertThat(outcome.score()).isEqualTo(100);
        assertThat(outcome.passed()).isTrue();
        QuizScorer.Outcome failed = QuizScorer.score(70, List.of(choice, multi), List.of(List.of(other), List.of(correct)));
        assertThat(failed.score()).isEqualTo(0);
        assertThat(failed.passed()).isFalse();
        assertThat(QuizScorer.score(50, List.of(choice, multi), List.of(List.of(correct), List.of(other))).score()).isEqualTo(50);
    }
}
