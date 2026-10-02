package com.mrsoft.arabicreference.morphology.domain;

import java.util.List;
import java.util.UUID;

public record AnalysisCandidate(
        String surfaceForm,
        String normalizedForm,
        String lemma,
        UUID lexicalEntryId,
        String root,
        UUID rootId,
        String patternCode,
        String patternOriginal,
        PatternCategory patternCategory,
        String partOfSpeech,
        MorphologicalFeatures features,
        Segmentation segmentation,
        DerivationKind derivation,
        AnalysisProvenance provenance,
        List<String> appliedRuleIds,
        List<String> explanationCodes) {

    public AnalysisCandidate {
        appliedRuleIds = appliedRuleIds == null ? List.of() : List.copyOf(appliedRuleIds);
        explanationCodes = explanationCodes == null ? List.of() : List.copyOf(explanationCodes);
        features = features == null ? MorphologicalFeatures.empty() : features;
    }
}
