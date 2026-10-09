package com.mrsoft.arabicreference.contentimport.domain;

import com.mrsoft.arabicreference.dictionary.domain.GrammaticalGender;
import com.mrsoft.arabicreference.dictionary.domain.PartOfSpeech;
import com.mrsoft.arabicreference.dictionary.domain.SemanticDomain;
import com.mrsoft.arabicreference.dictionary.domain.UsageLabel;
import com.mrsoft.arabicreference.dictionary.domain.FormType;
import java.util.List;

public record ValidatedLexicalRecord(
        ImportedLexicalRecord input,
        String normalizedLemma,
        String normalizedRoot,
        PartOfSpeech partOfSpeech,
        GrammaticalGender gender,
        UsageLabel usageLabel,
        SemanticDomain semanticDomain,
        Integer pageFrom,
        Integer pageTo,
        List<ValidatedSense> senses,
        List<ValidatedForm> forms) {

    public ValidatedLexicalRecord {
        senses = List.copyOf(senses);
        forms = List.copyOf(forms);
    }

    public record ValidatedSense(
            String definition,
            String shortDefinition,
            UsageLabel usageLabel,
            SemanticDomain semanticDomain,
            int displayOrder) {
    }

    public record ValidatedForm(FormType type, String original, String normalized) {
    }
}
