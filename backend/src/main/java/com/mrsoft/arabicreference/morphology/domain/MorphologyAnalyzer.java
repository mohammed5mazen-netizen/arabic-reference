package com.mrsoft.arabicreference.morphology.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Deterministic candidate generator. A published manual reading outranks a dictionary hit,
 * which outranks a rule. No root is emitted unless a published root record supports it.
 */
public final class MorphologyAnalyzer {

    private MorphologyAnalyzer() {
    }

    public static AnalysisReport analyze(
            String original,
            String normalized,
            PublishedLexicon lexicon,
            List<ManualReading> manuals,
            Set<String> enabledRules,
            int limit) {
        int bounded = Math.max(1, Math.min(limit, 25));
        Map<String, AnalysisCandidate> unique = new LinkedHashMap<>();
        Set<String> enabled = enabledRules == null ? Set.of() : enabledRules;
        if (manuals != null && enabled.contains("R-MANUAL")) {
            for (ManualReading manual : manuals) {
                add(unique, manualCandidate(original, normalized, manual));
            }
        }
        if (enabled.contains("R-DICT")) {
            for (Segmentation segmentation : CliticSegmenter.segment(normalized)) {
                if (!segmentationAllowed(segmentation, enabled)) {
                    continue;
                }
                for (PublishedLexicon.LexiconEntry entry : lexicon.lemmas(segmentation.stem())) {
                    add(unique, dictionaryCandidate(original, normalized, segmentation, entry));
                }
            }
        }
        addPatternCandidates(original, normalized, lexicon, unique, enabled);
        List<AnalysisCandidate> ordered = unique.values().stream()
                .sorted(Comparator
                        .comparingInt((AnalysisCandidate candidate) -> rank(candidate.provenance()))
                        .thenComparing(candidate -> candidate.lemma() == null ? "" : candidate.lemma())
                        .thenComparing(candidate -> candidate.partOfSpeech() == null ? "" : candidate.partOfSpeech())
                        .thenComparing(candidate -> String.join(",", candidate.explanationCodes())))
                .toList();
        boolean truncated = ordered.size() > bounded;
        List<AnalysisCandidate> kept = ordered.stream().limit(bounded).toList();
        AnalysisProvenance resultClass = kept.size() > 1 ? AnalysisProvenance.AMBIGUOUS : kept.isEmpty() ? null : kept.get(0).provenance();
        return new AnalysisReport(original, normalized, kept, truncated, bounded, RuleSet.VERSION, resultClass);
    }

    private static boolean segmentationAllowed(Segmentation segmentation, Set<String> enabled) {
        if (segmentation.prefixes().contains("ال") && !enabled.contains("R-AL")) {
            return false;
        }
        if (!segmentation.clitics().isEmpty()) {
            String clitic = segmentation.clitics().get(0);
            boolean conjunction = "و".equals(clitic) || "ف".equals(clitic);
            if (conjunction && !enabled.contains("R-CONJ")) {
                return false;
            }
            if (!conjunction && !enabled.contains("R-PREP")) {
                return false;
            }
        }
        return segmentation.suffixes().isEmpty() || enabled.contains("R-SUFFIX");
    }

    private static void addPatternCandidates(
            String original,
            String normalized,
            PublishedLexicon lexicon,
            Map<String, AnalysisCandidate> unique,
            Set<String> enabled) {
        String letters = ArabicLetters.lettersOnly(normalized);
        if (letters.codePointCount(0, letters.length()) == 4 && letters.codePointAt(Character.offsetByCodePoints(letters, 0, 1)) == 'ا') {
            String root = letter(letters, 0) + letter(letters, 2) + letter(letters, 3);
            if (enabled.contains("R-FA3IL")) {
                offer(original, normalized, lexicon, unique, root, "FA3IL", "فَاعِل", PatternCategory.ACTIVE_PARTICIPLE, DerivationKind.ACTIVE_PARTICIPLE, "R-FA3IL", "PUBLISHED_ROOT_ACTIVE_PARTICIPLE");
            }
        }
        if (letters.startsWith("م") && letters.codePointCount(0, letters.length()) == 5 && letters.codePointAt(Character.offsetByCodePoints(letters, 0, 3)) == 'و') {
            String root = letter(letters, 1) + letter(letters, 2) + letter(letters, 4);
            if (enabled.contains("R-MAF3UL")) {
                offer(original, normalized, lexicon, unique, root, "MAF3UL", "مَفْعُول", PatternCategory.PASSIVE_PARTICIPLE, DerivationKind.PASSIVE_PARTICIPLE, "R-MAF3UL", "PUBLISHED_ROOT_PASSIVE_PARTICIPLE");
            }
        }
    }

    private static void offer(
            String original,
            String normalized,
            PublishedLexicon lexicon,
            Map<String, AnalysisCandidate> unique,
            String rootLetters,
            String patternCode,
            String patternOriginal,
            PatternCategory category,
            DerivationKind derivation,
            String ruleId,
            String explanation) {
        lexicon.root(rootLetters).ifPresent(root -> {
            if (!PatternCompatibility.compatible(3, root.radicalCount())) {
                return;
            }
            boolean verb = lexicon.entriesForRoot(root.id()).stream().anyMatch(entry -> "VERB".equals(entry.partOfSpeech()));
            if (!verb) {
                return;
            }
            add(unique, new AnalysisCandidate(
                    original,
                    normalized,
                    null,
                    null,
                    root.original(),
                    root.id(),
                    patternCode,
                    patternOriginal,
                    category,
                    category == PatternCategory.ACTIVE_PARTICIPLE || category == PatternCategory.PASSIVE_PARTICIPLE ? "NOUN" : null,
                    MorphologicalFeatures.empty(),
                    Segmentation.identity(normalized),
                    derivation,
                    AnalysisProvenance.RULE_DERIVED,
                    List.of(ruleId),
                    List.of(explanation)));
        });
    }

    private static AnalysisCandidate dictionaryCandidate(
            String original,
            String normalized,
            Segmentation segmentation,
            PublishedLexicon.LexiconEntry entry) {
        List<String> rules = new ArrayList<>();
        List<String> explanations = new ArrayList<>();
        rules.add("R-DICT");
        explanations.add("DICTIONARY_LEMMA");
        if (segmentation.prefixes().contains("ال")) {
            rules.add("R-AL");
            explanations.add("DEFINITE_ARTICLE");
        }
        if (!segmentation.clitics().isEmpty()) {
            String clitic = segmentation.clitics().get(0);
            if ("و".equals(clitic) || "ف".equals(clitic)) {
                rules.add("R-CONJ");
                explanations.add("CONJUNCTION_CLITIC");
            } else {
                rules.add("R-PREP");
                explanations.add("PROCLITIC");
            }
        }
        if (!segmentation.suffixes().isEmpty()) {
            rules.add("R-SUFFIX");
            explanations.add("ATTACHED_PRONOUN");
        }
        MorphologicalFeatures features = segmentation.prefixes().contains("ال")
                ? new MorphologicalFeatures(null, null, null, null, null, null, null, MorphologicalFeatures.Definiteness.DEFINITE)
                : MorphologicalFeatures.empty();
        return new AnalysisCandidate(
                original,
                normalized,
                entry.lemmaOriginal(),
                entry.id(),
                entry.rootOriginal(),
                entry.rootId(),
                null,
                null,
                null,
                entry.partOfSpeech(),
                features,
                segmentation,
                null,
                AnalysisProvenance.EXACT_DICTIONARY,
                rules,
                explanations);
    }

    private static AnalysisCandidate manualCandidate(String original, String normalized, ManualReading manual) {
        return new AnalysisCandidate(
                original,
                normalized,
                manual.lemma(),
                manual.entryId(),
                manual.root(),
                manual.rootId(),
                manual.patternCode(),
                manual.patternOriginal(),
                manual.category(),
                manual.partOfSpeech(),
                manual.features(),
                manual.segmentation(),
                manual.derivation(),
                AnalysisProvenance.MANUAL_VERIFIED,
                List.of("R-MANUAL"),
                manual.explanationCodes().isEmpty() ? List.of("MANUAL_RECORD") : manual.explanationCodes());
    }

    private static void add(Map<String, AnalysisCandidate> unique, AnalysisCandidate candidate) {
        String key = (candidate.provenance() + "|" + candidate.lexicalEntryId() + "|" + candidate.patternCode() + "|"
                + (candidate.segmentation() == null ? "" : candidate.segmentation().stem()) + "|"
                + String.join(",", candidate.explanationCodes()));
        unique.putIfAbsent(key, candidate);
    }

    private static int rank(AnalysisProvenance provenance) {
        return switch (provenance) {
            case MANUAL_VERIFIED -> 0;
            case EXACT_DICTIONARY -> 1;
            case RULE_DERIVED -> 2;
            case AMBIGUOUS -> 3;
        };
    }

    private static String letter(String value, int index) {
        int offset = Character.offsetByCodePoints(value, 0, index);
        return value.substring(offset, offset + Character.charCount(value.codePointAt(offset)));
    }

    public record ManualReading(
            UUID entryId,
            String lemma,
            String partOfSpeech,
            UUID rootId,
            String root,
            String patternCode,
            String patternOriginal,
            PatternCategory category,
            DerivationKind derivation,
            MorphologicalFeatures features,
            Segmentation segmentation,
            List<String> explanationCodes) {

        public ManualReading {
            explanationCodes = explanationCodes == null ? List.of() : List.copyOf(explanationCodes);
            features = features == null ? MorphologicalFeatures.empty() : features;
            segmentation = segmentation == null ? Segmentation.identity(lemma) : segmentation;
        }
    }
}
