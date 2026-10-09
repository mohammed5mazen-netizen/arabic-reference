package com.mrsoft.arabicreference.contentimport;

import static org.assertj.core.api.Assertions.assertThat;

import com.mrsoft.arabicreference.contentimport.application.ImportValidator;
import com.mrsoft.arabicreference.contentimport.application.NormalizationService;
import com.mrsoft.arabicreference.contentimport.domain.ImportedLexicalRecord;
import com.mrsoft.arabicreference.linguistics.application.ArabicTextNormalizationService;
import org.junit.jupiter.api.Test;

class ImportValidatorTest {
    private final ImportValidator validator = new ImportValidator(new NormalizationService(new ArabicTextNormalizationService()));

    @Test
    void preservesDisplayTextAndNormalizesArabicSearchForm() {
        var result = validator.validate(record("\u0643\u0650\u062A\u0627\u0628", "\u0643\u062A\u0628", "\u0643\u064F\u062A\u064F\u0628", "Meaning supported by the source."), 1);

        assertThat(result.valid()).isTrue();
        assertThat(result.record().input().lemma()).isEqualTo("\u0643\u0650\u062A\u0627\u0628");
        assertThat(result.record().normalizedLemma()).isEqualTo("\u0643\u062A\u0627\u0628");
        assertThat(result.record().normalizedRoot()).isEqualTo("\u0643\u062A\u0628");
        assertThat(result.record().input().pluralForm()).isEqualTo("\u0643\u064F\u062A\u064F\u0628");
        assertThat(result.warnings()).isNotEmpty();
    }

    @Test
    void handlesAlefVariantsAndTatweelWithoutChangingDisplayText() {
        String lemma = "\u0622\u0645\u0640\u0640\u0640\u0640\u0627\u0644";
        var result = validator.validate(record(lemma, "\u0623\u0645\u0644", null, null), 1);

        assertThat(result.valid()).isTrue();
        assertThat(result.record().input().lemma()).isEqualTo(lemma);
        assertThat(result.record().normalizedLemma()).isEqualTo("\u0627\u0645\u0627\u0644");
    }

    @Test
    void invalidRecordsNeedAStableSourceLocatorAndEitherAValueOrVerifiedForm() {
        var missingSource = validator.validate(new ImportedLexicalRecord(
                "id", null, "\u0643\u062A\u0627\u0628", null, "NOUN", null, null, null, null, null, null, null, null, null, null), 3);
        var missingContent = validator.validate(record("\u0643\u062A\u0627\u0628", null, null, null), 4);

        assertThat(missingSource.issues()).extracting("code").contains("sourceLocator");
        assertThat(missingContent.issues()).extracting("code").contains("definition");
    }

    @Test
    void rejectsUnsupportedOrUnverifiedRootRelationships() {
        var result = validator.validate(record("\u0643\u062A\u0627\u0628", "\u0643\u062A\u06281", null, null), 1);

        assertThat(result.issues()).extracting("code").contains("root");
    }

    private static ImportedLexicalRecord record(String lemma, String root, String plural, String definition) {
        return new ImportedLexicalRecord(
                "source-entry-1",
                "https://example.test/entry/1",
                lemma,
                null,
                "NOUN",
                null,
                root,
                null,
                plural,
                definition,
                null,
                null,
                null,
                null,
                null);
    }
}
