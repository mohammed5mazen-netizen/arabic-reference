package com.mrsoft.arabicreference.linguistics.domain.editorial;

/**
 * Working-copy lifecycle for linguistic records.
 * A published snapshot can stay public while a later draft is reviewed.
 */
public enum PublicationStatus {
    DRAFT,
    IN_REVIEW,
    CHANGES_REQUESTED,
    VERIFIED,
    PUBLISHED,
    ARCHIVED
}
