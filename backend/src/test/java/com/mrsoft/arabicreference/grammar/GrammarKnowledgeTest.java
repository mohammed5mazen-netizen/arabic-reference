package com.mrsoft.arabicreference.grammar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mrsoft.arabicreference.grammar.domain.AnnotationConstraints;
import com.mrsoft.arabicreference.grammar.domain.ExampleConstraints;
import com.mrsoft.arabicreference.grammar.domain.ExampleType;
import com.mrsoft.arabicreference.grammar.domain.GrammaticalState;
import com.mrsoft.arabicreference.grammar.domain.StateKind;
import com.mrsoft.arabicreference.grammar.domain.TopicGraph;
import com.mrsoft.arabicreference.shared.kernel.exception.ConflictException;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GrammarKnowledgeTest {

    @Test
    void hierarchyRejectsSelfParentAndDescendantCycles() {
        UUID root = UUID.randomUUID();
        UUID child = UUID.randomUUID();
        Map<UUID, UUID> parents = Map.of(child, root);
        assertThatThrownBy(() -> TopicGraph.assertNoParentCycle(root, root, id -> Optional.ofNullable(parents.get(id))))
                .isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> TopicGraph.assertNoParentCycle(root, child, id -> Optional.ofNullable(parents.get(id))))
                .isInstanceOf(ConflictException.class);
        TopicGraph.assertNoParentCycle(child, root, id -> Optional.ofNullable(parents.get(id)));
    }

    @Test
    void prerequisitesRejectCycles() {
        UUID topic = UUID.randomUUID();
        UUID required = UUID.randomUUID();
        assertThatThrownBy(() -> TopicGraph.assertNoPrerequisiteCycle(topic, topic, id -> List.of()))
                .isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> TopicGraph.assertNoPrerequisiteCycle(topic, required, id -> id.equals(required) ? List.of(topic) : List.of()))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void quotedExamplesNeedCitationsAndConstructedExamplesDoNot() {
        assertThat(ExampleType.QUOTED.citationRequired()).isTrue();
        assertThat(ExampleType.QURANIC.citationRequired()).isTrue();
        assertThat(ExampleType.POETRY.citationRequired()).isTrue();
        assertThat(ExampleType.PROSE.citationRequired()).isTrue();
        assertThat(ExampleType.CONSTRUCTED.editorial()).isTrue();
        assertThat(ExampleType.COUNTEREXAMPLE.editorial()).isTrue();
        assertThatThrownBy(() -> ExampleConstraints.assertValid(ExampleType.QUOTED, null, null, null, null, null))
                .isInstanceOf(ValidationException.class);
        ExampleConstraints.assertValid(ExampleType.CONSTRUCTED, null, null, null, null, null);
        ExampleConstraints.assertValid(ExampleType.COUNTEREXAMPLE, null, null, null, null, null);
        assertThatThrownBy(() -> ExampleConstraints.assertValid(ExampleType.QURANIC, UUID.randomUUID(), null, 1, null, null))
                .isInstanceOf(ValidationException.class);
        ExampleConstraints.assertValid(ExampleType.QURANIC, UUID.randomUUID(), 1, 1, null, null);
        assertThatThrownBy(() -> ExampleConstraints.assertValid(ExampleType.POETRY, UUID.randomUUID(), null, null, "", "الديوان"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void nominalCaseRejectsJazmAndVerbalMoodRejectsJarr() {
        AnnotationConstraints.assertCompatible(StateKind.NOMINAL_CASE, GrammaticalState.RAFA);
        AnnotationConstraints.assertCompatible(StateKind.VERBAL_MOOD, GrammaticalState.JAZM);
        assertThatThrownBy(() -> AnnotationConstraints.assertCompatible(StateKind.NOMINAL_CASE, GrammaticalState.JAZM))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> AnnotationConstraints.assertCompatible(StateKind.VERBAL_MOOD, GrammaticalState.JARR))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void tokenPositionsMustBeUnique() {
        AnnotationConstraints.assertPositions(List.of(1, 0, 2));
        assertThatThrownBy(() -> AnnotationConstraints.assertPositions(List.of(0, 0)))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> AnnotationConstraints.assertPositions(List.of(-1)))
                .isInstanceOf(ValidationException.class);
    }
}
