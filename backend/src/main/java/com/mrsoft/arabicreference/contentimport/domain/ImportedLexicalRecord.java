package com.mrsoft.arabicreference.contentimport.domain;

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
        String pageTo) {
}
