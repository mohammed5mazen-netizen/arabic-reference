package com.mrsoft.arabicreference.linguistics.domain.editorial;

import com.mrsoft.arabicreference.shared.kernel.exception.ConflictException;

/**
 * Allowed editorial moves. Draft content cannot jump straight to publication.
 */
public final class EditorialWorkflow {

    private EditorialWorkflow() {
    }

    public static PublicationStatus submit(PublicationStatus current) {
        if (current != PublicationStatus.DRAFT && current != PublicationStatus.CHANGES_REQUESTED) {
            throw illegal(current, "submit");
        }
        return PublicationStatus.IN_REVIEW;
    }

    public static PublicationStatus verify(PublicationStatus current) {
        if (current != PublicationStatus.IN_REVIEW) {
            throw illegal(current, "verify");
        }
        return PublicationStatus.VERIFIED;
    }

    public static PublicationStatus requestChanges(PublicationStatus current) {
        if (current != PublicationStatus.IN_REVIEW) {
            throw illegal(current, "return");
        }
        return PublicationStatus.CHANGES_REQUESTED;
    }

    public static PublicationStatus publish(PublicationStatus current) {
        if (current != PublicationStatus.VERIFIED) {
            throw illegal(current, "publish");
        }
        return PublicationStatus.PUBLISHED;
    }

    public static PublicationStatus archive(PublicationStatus current) {
        if (current != PublicationStatus.PUBLISHED) {
            throw illegal(current, "archive");
        }
        return PublicationStatus.ARCHIVED;
    }

    public static boolean editable(PublicationStatus current) {
        return current == PublicationStatus.DRAFT
                || current == PublicationStatus.CHANGES_REQUESTED
                || current == PublicationStatus.PUBLISHED;
    }

    private static ConflictException illegal(PublicationStatus current, String action) {
        return new ConflictException("Cannot " + action + " content that is " + current + ".");
    }
}
