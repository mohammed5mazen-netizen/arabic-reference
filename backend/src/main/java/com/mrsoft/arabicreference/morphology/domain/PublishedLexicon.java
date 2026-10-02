package com.mrsoft.arabicreference.morphology.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Read port for published dictionary evidence. Morphology does not own lexical records.
 */
public interface PublishedLexicon {

    List<LexiconEntry> lemmas(String normalizedLemma);

    Optional<LexiconRoot> root(String normalizedRoot);

    List<LexiconEntry> entriesForRoot(UUID rootId);

    Optional<LexiconEntry> entry(UUID id);

    record LexiconEntry(
            UUID id,
            String lemmaOriginal,
            String lemmaNormalized,
            String vocalizedForm,
            UUID rootId,
            String rootOriginal,
            String rootNormalized,
            Integer radicalCount,
            String partOfSpeech,
            String slug) {
    }

    record LexiconRoot(UUID id, String original, String normalized, int radicalCount) {
    }
}
