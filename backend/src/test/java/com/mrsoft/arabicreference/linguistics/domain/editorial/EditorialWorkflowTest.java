package com.mrsoft.arabicreference.linguistics.domain.editorial;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mrsoft.arabicreference.shared.kernel.exception.ConflictException;
import org.junit.jupiter.api.Test;

class EditorialWorkflowTest {

    @Test
    void draftCannotJumpToPublished() {
        assertThat(EditorialWorkflow.submit(PublicationStatus.DRAFT)).isEqualTo(PublicationStatus.IN_REVIEW);
        assertThat(EditorialWorkflow.submit(PublicationStatus.CHANGES_REQUESTED)).isEqualTo(PublicationStatus.IN_REVIEW);
        assertThat(EditorialWorkflow.verify(PublicationStatus.IN_REVIEW)).isEqualTo(PublicationStatus.VERIFIED);
        assertThat(EditorialWorkflow.requestChanges(PublicationStatus.IN_REVIEW)).isEqualTo(PublicationStatus.CHANGES_REQUESTED);
        assertThat(EditorialWorkflow.publish(PublicationStatus.VERIFIED)).isEqualTo(PublicationStatus.PUBLISHED);
        assertThat(EditorialWorkflow.archive(PublicationStatus.PUBLISHED)).isEqualTo(PublicationStatus.ARCHIVED);
        assertThatThrownBy(() -> EditorialWorkflow.publish(PublicationStatus.DRAFT)).isInstanceOf(ConflictException.class);
        assertThatThrownBy(() -> EditorialWorkflow.verify(PublicationStatus.DRAFT)).isInstanceOf(ConflictException.class);
        assertThat(EditorialWorkflow.editable(PublicationStatus.PUBLISHED)).isTrue();
        assertThat(EditorialWorkflow.editable(PublicationStatus.IN_REVIEW)).isFalse();
    }
}
