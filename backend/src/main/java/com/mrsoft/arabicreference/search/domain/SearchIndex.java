package com.mrsoft.arabicreference.search.domain;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Write port for the published search index. Callers update it in the same transaction as publication.
 */
public interface SearchIndex {

    void exclusiveLock();

    boolean tryExclusiveLock();

    void upsert(SearchDocument document);

    void remove(SearchEntityType type, UUID entityId);

    void replaceAll(List<SearchDocument> documents, UUID actorId);

    SearchIndexState state();

    List<StoredDocument> storedDocuments();

    interface SearchIndexState {
        int indexVersion();

        long documentCount();

        Instant lastRebuildAt();
    }

    record StoredDocument(SearchEntityType type, UUID entityId, int indexVersion) {
    }
}
