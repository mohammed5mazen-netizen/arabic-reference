package com.mrsoft.arabicreference.search.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Read port for linguistic search. PostgreSQL backs the first adapter.
 * A later engine can replace the adapter without rewriting this contract.
 * The port stays read-only. It is not a source of linguistic truth.
 */
public interface LinguisticSearchPort {

    List<SearchCandidate> collect(String displayQuery, String searchKey, String foldedKey);

    List<SearchCandidate> fuzzy(String searchKey, double threshold, int limit);

    List<SearchCandidate> dictionaryWithRoot(String rootKey, int limit);

    Optional<SearchCandidate> dictionaryEntry(UUID entityId);

    List<SearchSuggestion> suggest(String searchKey, int limit);
}
