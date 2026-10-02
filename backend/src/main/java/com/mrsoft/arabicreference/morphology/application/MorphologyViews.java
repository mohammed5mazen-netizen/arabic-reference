package com.mrsoft.arabicreference.morphology.application;

import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import com.mrsoft.arabicreference.morphology.domain.CoverageStatus;
import com.mrsoft.arabicreference.morphology.domain.DerivationKind;
import com.mrsoft.arabicreference.morphology.domain.FormOrigin;
import com.mrsoft.arabicreference.morphology.domain.MorphologicalFeatures;
import com.mrsoft.arabicreference.morphology.domain.PatternCategory;
import com.mrsoft.arabicreference.morphology.domain.StemVowel;
import com.mrsoft.arabicreference.morphology.domain.VerbClass;
import java.util.List;
import java.util.UUID;

public final class MorphologyViews {

    private MorphologyViews() {
    }

    public record PageResult<T>(List<T> items, int page, int size, long total) {
    }

    public record PatternView(
            UUID id,
            String code,
            String patternOriginal,
            String patternNormalized,
            PatternCategory category,
            int radicalCount,
            String description,
            String status,
            long version) {
    }

    public record PatternDraft(String code, String patternOriginal, PatternCategory category, int radicalCount, String description) {
    }

    public record PatternUpdate(long version, String patternOriginal, PatternCategory category, int radicalCount, String description) {
    }

    public record AnalysisDraft(
            UUID lexicalEntryId,
            UUID patternId,
            DerivationKind derivation,
            VerbClass verbClass,
            StemVowel imperfectVowel,
            String notes,
            MorphologicalFeatures features) {
    }

    public record AnalysisUpdate(long version, AnalysisDraft draft) {
    }

    public record VersionRequest(long version) {
    }

    public record ReasonRequest(long version, String reason) {
    }

    public record CitationRequest(long version, UUID citationId) {
    }

    public record AnalysisView(
            UUID id,
            UUID lexicalEntryId,
            String lemma,
            UUID patternId,
            String patternCode,
            String patternOriginal,
            PatternCategory patternCategory,
            DerivationKind derivation,
            VerbClass verbClass,
            StemVowel imperfectVowel,
            String notes,
            MorphologicalFeatures features,
            PublicationStatus status,
            List<UUID> citationIds,
            long version) {
    }

    public record RuleView(String code, String explanationCode, String description, boolean enabled, String ruleSetVersion, long version) {
    }

    public record RuleChange(boolean enabled, long version) {
    }

    public record Reading(
            UUID analysisId,
            String patternCode,
            String patternOriginal,
            PatternCategory patternCategory,
            DerivationKind derivation,
            VerbClass verbClass,
            StemVowel imperfectVowel,
            String notes,
            MorphologicalFeatures features,
            List<UUID> citationIds) {
    }

    public record EntryMorphology(UUID entryId, List<Reading> readings, List<String> recordedPlurals) {
    }

    public record RecordedPattern(String code, String original, String lemma, String entrySlug, String derivation) {
    }

    public record RootMorphology(String root, String slug, List<RecordedPattern> patterns) {
    }

    public record ConjugatedForm(String surface, MorphologicalFeatures features, FormOrigin origin) {
    }

    public record ConjugationView(
            UUID entryId,
            CoverageStatus coverage,
            String verbClass,
            String patternCode,
            String ruleSetVersion,
            String reason,
            List<ConjugatedForm> forms) {
    }

    public record CoverageRow(String feature, String support, String note) {
    }

    public record CoverageView(String ruleSetVersion, List<CoverageRow> rows) {
    }
}
