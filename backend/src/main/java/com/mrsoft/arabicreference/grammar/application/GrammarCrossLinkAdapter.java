package com.mrsoft.arabicreference.grammar.application;

import com.mrsoft.arabicreference.dictionary.application.PublishedDictionaryQuery;
import com.mrsoft.arabicreference.grammar.domain.GrammarCrossLinks;
import com.mrsoft.arabicreference.morphology.application.PublishedMorphologyQuery;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class GrammarCrossLinkAdapter implements GrammarCrossLinks {

    private final PublishedDictionaryQuery dictionary;
    private final PublishedMorphologyQuery morphology;

    public GrammarCrossLinkAdapter(PublishedDictionaryQuery dictionary, PublishedMorphologyQuery morphology) {
        this.dictionary = dictionary;
        this.morphology = morphology;
    }

    @Override
    public Optional<LexicalLink> entry(UUID id) {
        return dictionary.lemma(id).map(lemma -> new LexicalLink(lemma.id(), lemma.lemmaOriginal(), lemma.slug()));
    }

    @Override
    public Optional<MorphologyLink> morphology(UUID analysisId) {
        return morphology.reading(analysisId).map(reading -> new MorphologyLink(reading.id(), reading.patternOriginal()));
    }

    @Override
    public boolean knownEntry(UUID id) {
        return dictionary.editableEntry(id).isPresent();
    }

    @Override
    public boolean knownMorphology(UUID id) {
        return morphology.exists(id);
    }
}
