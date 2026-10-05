package com.mrsoft.arabicreference.grammar.domain;

import java.util.Optional;
import java.util.UUID;

/**
 * Published dictionary and morphology facts used when a grammar snapshot is built.
 * Grammar domain code does not call those modules.
 */
public interface GrammarCrossLinks {

    Optional<LexicalLink> entry(UUID id);

    Optional<MorphologyLink> morphology(UUID analysisId);

    boolean knownEntry(UUID id);

    boolean knownMorphology(UUID id);

    record LexicalLink(UUID id, String lemma, String slug) {
    }

    record MorphologyLink(UUID id, String patternOriginal) {
    }
}
