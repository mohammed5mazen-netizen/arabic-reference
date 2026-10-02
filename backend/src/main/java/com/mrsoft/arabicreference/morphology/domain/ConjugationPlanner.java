package com.mrsoft.arabicreference.morphology.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * S3 conjugates only a sound triliteral فَعَلَ verb whose class and, for the imperfect, stem vowel are recorded.
 */
public final class ConjugationPlanner {

    private ConjugationPlanner() {
    }

    public static ConjugationReport plan(VerbClass verbClass, String patternCode, StemVowel vowel, String rootNormalized) {
        if (verbClass != VerbClass.SOUND || !"FA3ALA".equals(patternCode) || !threeRadicals(rootNormalized)) {
            return new ConjugationReport(CoverageStatus.UNSUPPORTED, verbClass, RuleSet.VERSION, List.of(), "VERB_OUTSIDE_S3_COVERAGE");
        }
        String first = radical(rootNormalized, 0);
        String second = radical(rootNormalized, 1);
        String third = radical(rootNormalized, 2);
        List<SoundVerbParadigm.ConjugatedCell> forms = new ArrayList<>(SoundVerbParadigm.perfect(first, second, third));
        if (vowel == null) {
            return new ConjugationReport(CoverageStatus.PARTIALLY_SUPPORTED, verbClass, RuleSet.VERSION, forms, "IMPERFECT_VOWEL_NOT_RECORDED");
        }
        forms.addAll(SoundVerbParadigm.imperfect(first, second, third, vowel));
        forms.addAll(SoundVerbParadigm.imperative(first, second, third, vowel));
        return new ConjugationReport(CoverageStatus.SUPPORTED, verbClass, RuleSet.VERSION, forms, null);
    }

    private static boolean threeRadicals(String root) {
        return root != null && root.codePointCount(0, root.length()) == 3 && ArabicLetters.words(root);
    }

    private static String radical(String root, int index) {
        int offset = Character.offsetByCodePoints(root, 0, index);
        return root.substring(offset, offset + Character.charCount(root.codePointAt(offset)));
    }

    public record ConjugationReport(
            CoverageStatus coverage,
            VerbClass verbClass,
            String ruleSetVersion,
            List<SoundVerbParadigm.ConjugatedCell> forms,
            String reason) {
    }
}
