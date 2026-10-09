package com.mrsoft.arabicreference.contentimport.application;

import com.mrsoft.arabicreference.contentimport.domain.ImportIssue;
import com.mrsoft.arabicreference.contentimport.domain.ImportedLexicalRecord;
import com.mrsoft.arabicreference.contentimport.domain.ValidatedLexicalRecord;
import com.mrsoft.arabicreference.dictionary.domain.ArabicLexicalText;
import com.mrsoft.arabicreference.dictionary.domain.GrammaticalGender;
import com.mrsoft.arabicreference.dictionary.domain.PartOfSpeech;
import com.mrsoft.arabicreference.dictionary.domain.SemanticDomain;
import com.mrsoft.arabicreference.dictionary.domain.UsageLabel;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ImportValidator {
    private final NormalizationService normalization;

    public ImportValidator(NormalizationService normalization) {
        this.normalization = normalization;
    }

    public Result validate(ImportedLexicalRecord record, int recordNumber) {
        List<ImportIssue> issues = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        requireText(record.recordKey(), "recordKey", 160, recordNumber, issues);
        requireText(record.sourceLocator(), "sourceLocator", 200, recordNumber, issues);
        requireText(record.lemma(), "lemma", 80, recordNumber, issues);
        if ((record.definition() == null || record.definition().isBlank())
                && (record.root() == null || record.root().isBlank())
                && (record.pluralForm() == null || record.pluralForm().isBlank())) {
            issue(issues, recordNumber, record.recordKey(), "definition", "A definition, verified root, or source-provided plural form is required.");
        }
        if (record.definition() != null && record.definition().codePointCount(0, record.definition().length()) > 4000) {
            issue(issues, recordNumber, record.recordKey(), "definition", "Definition exceeds 4000 characters.");
        }
        if (record.shortDefinition() != null && record.shortDefinition().codePointCount(0, record.shortDefinition().length()) > 280) {
            issue(issues, recordNumber, record.recordKey(), "shortDefinition", "Short definition exceeds 280 characters.");
        }
        if (record.vocalizedForm() != null && record.vocalizedForm().codePointCount(0, record.vocalizedForm().length()) > 80) {
            issue(issues, recordNumber, record.recordKey(), "vocalizedForm", "Vocalized form exceeds 80 characters.");
        }
        if (record.pluralForm() != null && record.pluralForm().codePointCount(0, record.pluralForm().length()) > 80) {
            issue(issues, recordNumber, record.recordKey(), "pluralForm", "Plural form exceeds 80 characters.");
        }
        PartOfSpeech partOfSpeech = enumValue(record.partOfSpeech(), PartOfSpeech.class, "partOfSpeech", recordNumber, record.recordKey(), issues, true);
        GrammaticalGender gender = enumValue(record.gender(), GrammaticalGender.class, "gender", recordNumber, record.recordKey(), issues, false);
        UsageLabel usageLabel = enumValue(record.usageLabel(), UsageLabel.class, "usageLabel", recordNumber, record.recordKey(), issues, false);
        SemanticDomain semanticDomain = enumValue(record.semanticDomain(), SemanticDomain.class, "semanticDomain", recordNumber, record.recordKey(), issues, false);

        String normalizedLemma = null;
        if (record.lemma() != null && !record.lemma().isBlank()) {
            normalizedLemma = normalization.searchForm(record.lemma().trim());
            try {
                ArabicLexicalText.requireLemma(normalizedLemma);
            } catch (ValidationException exception) {
                issue(issues, recordNumber, record.recordKey(), "lemma", "Lemma must contain Arabic letters only.");
            }
            if (!normalizedLemma.equals(record.lemma().trim())) {
                warnings.add("Lemma was normalized for search; the original display form is preserved.");
            }
        }

        String normalizedRoot = null;
        if (record.root() != null && !record.root().isBlank()) {
            normalizedRoot = normalization.searchForm(record.root().trim());
            try {
                ArabicLexicalText.requireRoot(normalizedRoot, record.rootNote());
            } catch (ValidationException exception) {
                issue(issues, recordNumber, record.recordKey(), "root", "Root must be verified Arabic letters; non-triliteral roots need a note.");
            }
            if (!normalizedRoot.equals(record.root().trim())) {
                warnings.add("Root was normalized; the original root form remains in the import source.");
            }
        }
        if (record.pluralForm() != null && !record.pluralForm().isBlank()) {
            String normalizedPlural = normalization.searchForm(record.pluralForm().trim());
            try {
                ArabicLexicalText.requireLemma(normalizedPlural);
            } catch (ValidationException exception) {
                issue(issues, recordNumber, record.recordKey(), "pluralForm", "Plural form must contain Arabic letters only.");
            }
        }
        Integer pageFrom = pageNumber(record.pageFrom(), "pageFrom", recordNumber, record.recordKey(), issues);
        Integer pageTo = pageNumber(record.pageTo(), "pageTo", recordNumber, record.recordKey(), issues);
        if (pageFrom != null && pageTo != null && pageTo < pageFrom) {
            issue(issues, recordNumber, record.recordKey(), "pageTo", "pageTo must be greater than or equal to pageFrom.");
        }
        if (issues.isEmpty()) {
            return new Result(new ValidatedLexicalRecord(
                    record, normalizedLemma, normalizedRoot, partOfSpeech, gender, usageLabel, semanticDomain, pageFrom, pageTo),
                    List.of(), warnings);
        }
        return new Result(null, List.copyOf(issues), warnings);
    }

    private static Integer pageNumber(String value, String field, int row, String key, List<ImportIssue> issues) {
        if (value == null || value.isBlank()) return null;
        try {
            int page = Integer.parseInt(value.trim());
            if (page < 1) throw new NumberFormatException();
            return page;
        } catch (NumberFormatException exception) {
            issue(issues, row, key, field, field + " must be a positive integer.");
            return null;
        }
    }

    private static <E extends Enum<E>> E enumValue(
            String value, Class<E> type, String field, int row, String key, List<ImportIssue> issues, boolean required) {
        if (value == null || value.isBlank()) {
            if (required) issue(issues, row, key, field, field + " is required.");
            return null;
        }
        try {
            return Enum.valueOf(type, value.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            issue(issues, row, key, field, "Unsupported " + field + " value.");
            return null;
        }
    }

    private static void requireText(String value, String field, int max, int row, List<ImportIssue> issues) {
        if (value == null || value.isBlank()) {
            issue(issues, row, null, field, field + " is required.");
        } else if (value.codePointCount(0, value.length()) > max) {
            issue(issues, row, null, field, field + " exceeds " + max + " characters.");
        }
    }

    private static void issue(List<ImportIssue> issues, int row, String key, String code, String message) {
        issues.add(new ImportIssue(row, key, code, message));
    }

    public record Result(ValidatedLexicalRecord record, List<ImportIssue> issues, List<String> warnings) {
        public Result {
            issues = List.copyOf(issues);
            warnings = List.copyOf(warnings);
        }

        public boolean valid() {
            return record != null;
        }
    }
}
