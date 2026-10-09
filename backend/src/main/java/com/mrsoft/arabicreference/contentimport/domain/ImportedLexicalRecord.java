package com.mrsoft.arabicreference.contentimport.domain;

import java.util.List;

public record ImportedLexicalRecord(
        String recordKey,
        String sourceLocator,
        String lemma,
        String vocalizedForm,
        String partOfSpeech,
        String gender,
        String root,
        String rootNote,
        String pluralForm,
        String definition,
        String shortDefinition,
        String usageLabel,
        String semanticDomain,
        String pageFrom,
        String pageTo,
        List<ImportedSense> senses,
        List<ImportedForm> forms) {

    public ImportedLexicalRecord {
        senses = senses == null ? List.of() : List.copyOf(senses);
        forms = forms == null ? List.of() : List.copyOf(forms);
    }

    public ImportedLexicalRecord(
            String recordKey,
            String sourceLocator,
            String lemma,
            String vocalizedForm,
            String partOfSpeech,
            String gender,
            String root,
            String rootNote,
            String pluralForm,
            String definition,
            String shortDefinition,
            String usageLabel,
            String semanticDomain,
            String pageFrom,
            String pageTo) {
        this(recordKey, sourceLocator, lemma, vocalizedForm, partOfSpeech, gender, root, rootNote, pluralForm,
                definition, shortDefinition, usageLabel, semanticDomain, pageFrom, pageTo, List.of(), List.of());
    }

    public record ImportedSense(
            String definition,
            String shortDefinition,
            String usageLabel,
            String semanticDomain,
            Integer displayOrder) {
    }

    public record ImportedForm(String type, String original, String normalized) {
    }
}
