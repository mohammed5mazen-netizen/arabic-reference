package com.mrsoft.arabicreference.morphology.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mrsoft.arabicreference.morphology.domain.MorphologicalFeatures.FeatureGender;
import com.mrsoft.arabicreference.morphology.domain.MorphologicalFeatures.GrammaticalNumber;
import com.mrsoft.arabicreference.morphology.domain.MorphologicalFeatures.Person;
import com.mrsoft.arabicreference.morphology.domain.MorphologicalFeatures.VerbAspect;
import com.mrsoft.arabicreference.morphology.domain.MorphologyAnalyzer.ManualReading;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MorphologyEngineTest {

    private static final Set<String> ENABLED = Set.of("R-DICT", "R-AL", "R-CONJ", "R-PREP", "R-SUFFIX", "R-FA3IL", "R-MAF3UL", "R-MANUAL");

    @Test
    void soundVerbsConjugateOnlyWhenTheClassAndVowelAreKnown() {
        assertThat(form(SoundVerbParadigm.perfect("ك", "ت", "ب"), Person.THIRD, GrammaticalNumber.SINGULAR, FeatureGender.MASCULINE, VerbAspect.PERFECT))
                .isEqualTo("كَتَبَ");
        assertThat(form(SoundVerbParadigm.perfect("ك", "ت", "ب"), Person.THIRD, GrammaticalNumber.DUAL, FeatureGender.MASCULINE, VerbAspect.PERFECT))
                .isEqualTo("كَتَبَا");
        assertThat(form(SoundVerbParadigm.imperfect("ك", "ت", "ب", StemVowel.DAMMA), Person.THIRD, GrammaticalNumber.SINGULAR, FeatureGender.MASCULINE, VerbAspect.IMPERFECT))
                .isEqualTo("يَكْتُبُ");
        assertThat(form(SoundVerbParadigm.imperative("ك", "ت", "ب", StemVowel.DAMMA), Person.SECOND, GrammaticalNumber.SINGULAR, FeatureGender.MASCULINE, VerbAspect.IMPERATIVE))
                .isEqualTo("اُكْتُبْ");
        assertThat(form(SoundVerbParadigm.imperfect("ج", "ل", "س", StemVowel.KASRA), Person.THIRD, GrammaticalNumber.SINGULAR, FeatureGender.MASCULINE, VerbAspect.IMPERFECT))
                .isEqualTo("يَجْلِسُ");
        assertThat(form(SoundVerbParadigm.imperfect("ف", "ت", "ح", StemVowel.FATHA), Person.FIRST, GrammaticalNumber.PLURAL, FeatureGender.NOT_APPLICABLE, VerbAspect.IMPERFECT))
                .isEqualTo("نَفْتَحُ");

        assertThat(ConjugationPlanner.plan(VerbClass.HOLLOW, "FA3ALA", StemVowel.DAMMA, "قول").coverage())
                .isEqualTo(CoverageStatus.UNSUPPORTED);
        assertThat(ConjugationPlanner.plan(VerbClass.HOLLOW, "FA3ALA", StemVowel.DAMMA, "قول").forms()).isEmpty();
        assertThat(ConjugationPlanner.plan(VerbClass.SOUND, "FA3ALA", null, "كتب").coverage())
                .isEqualTo(CoverageStatus.PARTIALLY_SUPPORTED);
        assertThat(ConjugationPlanner.plan(VerbClass.SOUND, "FA33ALA", StemVowel.DAMMA, "كتب").coverage())
                .isEqualTo(CoverageStatus.UNSUPPORTED);
    }

    @Test
    void analysisKeepsEverySupportedCandidateAndDoesNotInventARoot() {
        UUID noun = UUID.randomUUID();
        UUID verb = UUID.randomUUID();
        UUID root = UUID.randomUUID();
        MemoryLexicon lexicon = new MemoryLexicon(List.of(
                entry(noun, "كتاب", "NOUN", root, "كتب"),
                entry(verb, "كتب", "VERB", root, "كتب")),
                new PublishedLexicon.LexiconRoot(root, "كتب", "كتب", 3));
        AnalysisReport report = MorphologyAnalyzer.analyze("والكتاب", "والكتاب", lexicon, List.of(), ENABLED, 8);
        assertThat(report.analyses()).extracting(AnalysisCandidate::provenance)
                .contains(AnalysisProvenance.EXACT_DICTIONARY);
        assertThat(report.analyses()).anyMatch(candidate -> candidate.segmentation().prefixes().contains("ال")
                && candidate.segmentation().clitics().contains("و")
                && noun.equals(candidate.lexicalEntryId()));
        MemoryLexicon both = new MemoryLexicon(List.of(
                entry(noun, "كتب", "NOUN", root, "كتب"),
                entry(verb, "كتب", "VERB", root, "كتب")),
                new PublishedLexicon.LexiconRoot(root, "كتب", "كتب", 3));
        AnalysisReport ambiguous = MorphologyAnalyzer.analyze("كتب", "كتب", both, List.of(), ENABLED, 8);
        assertThat(ambiguous.analyses()).hasSizeGreaterThan(1);
        assertThat(ambiguous.resultClass()).isEqualTo(AnalysisProvenance.AMBIGUOUS);

        AnalysisReport unknown = MorphologyAnalyzer.analyze("xyz", "xyz", lexicon, List.of(), ENABLED, 8);
        assertThat(unknown.analyses()).isEmpty();
        AnalysisReport katib = MorphologyAnalyzer.analyze("كاتب", "كاتب", lexicon, List.of(), ENABLED, 8);
        assertThat(katib.analyses()).anyMatch(candidate -> candidate.provenance() == AnalysisProvenance.RULE_DERIVED
                && root.equals(candidate.rootId())
                && "فَاعِل".equals(candidate.patternOriginal()));

        MemoryLexicon empty = new MemoryLexicon(List.of(), null);
        assertThat(MorphologyAnalyzer.analyze("كاتب", "كاتب", empty, List.of(), ENABLED, 8).analyses()).isEmpty();
    }

    @Test
    void manualReadingOutranksTheDictionaryAndCandidatesStayBounded() {
        UUID id = UUID.randomUUID();
        MemoryLexicon lexicon = new MemoryLexicon(List.of(entry(id, "كتاب", "NOUN", null, null)), null);
        ManualReading manual = new ManualReading(id, "كتاب", "NOUN", null, null, "FA3IL", "فَاعِل", PatternCategory.NOUN, null,
                MorphologicalFeatures.empty(), Segmentation.identity("كتاب"), List.of("MANUAL_RECORD"));
        AnalysisReport report = MorphologyAnalyzer.analyze("كتاب", "كتاب", lexicon, List.of(manual), ENABLED, 1);
        assertThat(report.analyses()).hasSize(1);
        assertThat(report.truncated()).isTrue();
        assertThat(report.analyses().get(0).provenance()).isEqualTo(AnalysisProvenance.MANUAL_VERIFIED);
        assertThat(report.ruleSetVersion()).isEqualTo(RuleSet.VERSION);
    }

    @Test
    void triliteralPatternRejectsAQuadriliteralRoot() {
        assertThat(PatternCompatibility.compatible(3, 4)).isFalse();
        assertThatThrownBy(() -> PatternCompatibility.require(3, 4)).isInstanceOf(ValidationException.class);
    }

    private static String form(
            List<SoundVerbParadigm.ConjugatedCell> cells,
            Person person,
            GrammaticalNumber number,
            FeatureGender gender,
            VerbAspect aspect) {
        return cells.stream()
                .filter(cell -> cell.features().person() == person
                        && cell.features().number() == number
                        && cell.features().gender() == gender
                        && cell.features().aspect() == aspect)
                .findFirst()
                .orElseThrow()
                .surface();
    }

    private static PublishedLexicon.LexiconEntry entry(UUID id, String lemma, String pos, UUID rootId, String root) {
        return new PublishedLexicon.LexiconEntry(id, lemma, lemma, null, rootId, root, root, root == null ? null : 3, pos, lemma + "-test");
    }

    private record MemoryLexicon(List<PublishedLexicon.LexiconEntry> entries, PublishedLexicon.LexiconRoot knownRoot) implements PublishedLexicon {
        @Override
        public List<LexiconEntry> lemmas(String normalizedLemma) {
            return entries.stream().filter(entry -> entry.lemmaNormalized().equals(normalizedLemma)).toList();
        }

        @Override
        public Optional<LexiconRoot> root(String normalizedRoot) {
            return knownRoot != null && knownRoot.normalized().equals(normalizedRoot) ? Optional.of(knownRoot) : Optional.empty();
        }

        @Override
        public List<LexiconEntry> entriesForRoot(UUID rootId) {
            return entries.stream().filter(entry -> rootId.equals(entry.rootId())).toList();
        }

        @Override
        public Optional<LexiconEntry> entry(UUID id) {
            return entries.stream().filter(entry -> entry.id().equals(id)).findFirst();
        }
    }
}
