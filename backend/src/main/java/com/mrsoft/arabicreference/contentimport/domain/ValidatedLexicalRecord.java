package com.mrsoft.arabicreference.contentimport.domain;

import com.mrsoft.arabicreference.dictionary.domain.GrammaticalGender;
import com.mrsoft.arabicreference.dictionary.domain.PartOfSpeech;
import com.mrsoft.arabicreference.dictionary.domain.SemanticDomain;
import com.mrsoft.arabicreference.dictionary.domain.UsageLabel;

public record ValidatedLexicalRecord(
        ImportedLexicalRecord input,
        String normalizedLemma,
        String normalizedRoot,
        PartOfSpeech partOfSpeech,
        GrammaticalGender gender,
        UsageLabel usageLabel,
        SemanticDomain semanticDomain,
        Integer pageFrom,
        Integer pageTo) {
}
