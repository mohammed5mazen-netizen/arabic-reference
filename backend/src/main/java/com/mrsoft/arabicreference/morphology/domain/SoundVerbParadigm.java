package com.mrsoft.arabicreference.morphology.domain;

import com.mrsoft.arabicreference.morphology.domain.MorphologicalFeatures.FeatureGender;
import com.mrsoft.arabicreference.morphology.domain.MorphologicalFeatures.GrammaticalNumber;
import com.mrsoft.arabicreference.morphology.domain.MorphologicalFeatures.Person;
import com.mrsoft.arabicreference.morphology.domain.MorphologicalFeatures.VerbAspect;
import com.mrsoft.arabicreference.morphology.domain.MorphologicalFeatures.VerbMood;
import com.mrsoft.arabicreference.morphology.domain.MorphologicalFeatures.VerbVoice;
import java.util.List;

/**
 * Active conjugation of a sound triliteral فَعَلَ verb.
 * The imperfect vowel must be supplied. It is not predicted.
 */
public final class SoundVerbParadigm {

    private SoundVerbParadigm() {
    }

    public static List<ConjugatedCell> perfect(String first, String second, String third) {
        String base = v(first) + "َ" + v(second) + "َ" + v(third);
        return List.of(
                cell(Person.THIRD, GrammaticalNumber.SINGULAR, FeatureGender.MASCULINE, VerbAspect.PERFECT, VerbMood.NOT_APPLICABLE, base + "َ"),
                cell(Person.THIRD, GrammaticalNumber.SINGULAR, FeatureGender.FEMININE, VerbAspect.PERFECT, VerbMood.NOT_APPLICABLE, base + "َتْ"),
                cell(Person.THIRD, GrammaticalNumber.DUAL, FeatureGender.MASCULINE, VerbAspect.PERFECT, VerbMood.NOT_APPLICABLE, base + "َا"),
                cell(Person.THIRD, GrammaticalNumber.DUAL, FeatureGender.FEMININE, VerbAspect.PERFECT, VerbMood.NOT_APPLICABLE, base + "َتَا"),
                cell(Person.THIRD, GrammaticalNumber.PLURAL, FeatureGender.MASCULINE, VerbAspect.PERFECT, VerbMood.NOT_APPLICABLE, base + "ُوا"),
                cell(Person.THIRD, GrammaticalNumber.PLURAL, FeatureGender.FEMININE, VerbAspect.PERFECT, VerbMood.NOT_APPLICABLE, base + "ْنَ"),
                cell(Person.SECOND, GrammaticalNumber.SINGULAR, FeatureGender.MASCULINE, VerbAspect.PERFECT, VerbMood.NOT_APPLICABLE, base + "ْتَ"),
                cell(Person.SECOND, GrammaticalNumber.SINGULAR, FeatureGender.FEMININE, VerbAspect.PERFECT, VerbMood.NOT_APPLICABLE, base + "ْتِ"),
                cell(Person.SECOND, GrammaticalNumber.DUAL, FeatureGender.NOT_APPLICABLE, VerbAspect.PERFECT, VerbMood.NOT_APPLICABLE, base + "ْتُمَا"),
                cell(Person.SECOND, GrammaticalNumber.PLURAL, FeatureGender.MASCULINE, VerbAspect.PERFECT, VerbMood.NOT_APPLICABLE, base + "ْتُمْ"),
                cell(Person.SECOND, GrammaticalNumber.PLURAL, FeatureGender.FEMININE, VerbAspect.PERFECT, VerbMood.NOT_APPLICABLE, base + "ْتُنَّ"),
                cell(Person.FIRST, GrammaticalNumber.SINGULAR, FeatureGender.NOT_APPLICABLE, VerbAspect.PERFECT, VerbMood.NOT_APPLICABLE, base + "ْتُ"),
                cell(Person.FIRST, GrammaticalNumber.PLURAL, FeatureGender.NOT_APPLICABLE, VerbAspect.PERFECT, VerbMood.NOT_APPLICABLE, base + "ْنَا"));
    }

    public static List<ConjugatedCell> imperfect(String first, String second, String third, StemVowel vowel) {
        String stem = v(first) + "ْ" + v(second) + mark(vowel) + v(third);
        String paused = stem + "ْ";
        return List.of(
                cell(Person.THIRD, GrammaticalNumber.SINGULAR, FeatureGender.MASCULINE, VerbAspect.IMPERFECT, VerbMood.INDICATIVE, "يَ" + stem + "ُ"),
                cell(Person.THIRD, GrammaticalNumber.SINGULAR, FeatureGender.FEMININE, VerbAspect.IMPERFECT, VerbMood.INDICATIVE, "تَ" + stem + "ُ"),
                cell(Person.THIRD, GrammaticalNumber.DUAL, FeatureGender.MASCULINE, VerbAspect.IMPERFECT, VerbMood.INDICATIVE, "يَ" + stem + "َانِ"),
                cell(Person.THIRD, GrammaticalNumber.DUAL, FeatureGender.FEMININE, VerbAspect.IMPERFECT, VerbMood.INDICATIVE, "تَ" + stem + "َانِ"),
                cell(Person.THIRD, GrammaticalNumber.PLURAL, FeatureGender.MASCULINE, VerbAspect.IMPERFECT, VerbMood.INDICATIVE, "يَ" + stem + "ُونَ"),
                cell(Person.THIRD, GrammaticalNumber.PLURAL, FeatureGender.FEMININE, VerbAspect.IMPERFECT, VerbMood.INDICATIVE, "يَ" + paused + "نَ"),
                cell(Person.SECOND, GrammaticalNumber.SINGULAR, FeatureGender.MASCULINE, VerbAspect.IMPERFECT, VerbMood.INDICATIVE, "تَ" + stem + "ُ"),
                cell(Person.SECOND, GrammaticalNumber.SINGULAR, FeatureGender.FEMININE, VerbAspect.IMPERFECT, VerbMood.INDICATIVE, "تَ" + stem + "ِينَ"),
                cell(Person.SECOND, GrammaticalNumber.DUAL, FeatureGender.NOT_APPLICABLE, VerbAspect.IMPERFECT, VerbMood.INDICATIVE, "تَ" + stem + "َانِ"),
                cell(Person.SECOND, GrammaticalNumber.PLURAL, FeatureGender.MASCULINE, VerbAspect.IMPERFECT, VerbMood.INDICATIVE, "تَ" + stem + "ُونَ"),
                cell(Person.SECOND, GrammaticalNumber.PLURAL, FeatureGender.FEMININE, VerbAspect.IMPERFECT, VerbMood.INDICATIVE, "تَ" + paused + "نَ"),
                cell(Person.FIRST, GrammaticalNumber.SINGULAR, FeatureGender.NOT_APPLICABLE, VerbAspect.IMPERFECT, VerbMood.INDICATIVE, "أَ" + stem + "ُ"),
                cell(Person.FIRST, GrammaticalNumber.PLURAL, FeatureGender.NOT_APPLICABLE, VerbAspect.IMPERFECT, VerbMood.INDICATIVE, "نَ" + stem + "ُ"));
    }

    public static List<ConjugatedCell> imperative(String first, String second, String third, StemVowel vowel) {
        String stem = v(first) + "ْ" + v(second) + mark(vowel) + v(third);
        String prosthetic = vowel == StemVowel.DAMMA ? "اُ" : "اِ";
        return List.of(
                cell(Person.SECOND, GrammaticalNumber.SINGULAR, FeatureGender.MASCULINE, VerbAspect.IMPERATIVE, VerbMood.NOT_APPLICABLE, prosthetic + stem + "ْ"),
                cell(Person.SECOND, GrammaticalNumber.SINGULAR, FeatureGender.FEMININE, VerbAspect.IMPERATIVE, VerbMood.NOT_APPLICABLE, prosthetic + stem + "ِي"),
                cell(Person.SECOND, GrammaticalNumber.DUAL, FeatureGender.NOT_APPLICABLE, VerbAspect.IMPERATIVE, VerbMood.NOT_APPLICABLE, prosthetic + stem + "َا"),
                cell(Person.SECOND, GrammaticalNumber.PLURAL, FeatureGender.MASCULINE, VerbAspect.IMPERATIVE, VerbMood.NOT_APPLICABLE, prosthetic + stem + "ُوا"),
                cell(Person.SECOND, GrammaticalNumber.PLURAL, FeatureGender.FEMININE, VerbAspect.IMPERATIVE, VerbMood.NOT_APPLICABLE, prosthetic + stem + "ْنَ"));
    }

    private static String mark(StemVowel vowel) {
        return switch (vowel) {
            case FATHA -> "َ";
            case KASRA -> "ِ";
            case DAMMA -> "ُ";
        };
    }

    private static String v(String letter) {
        if (letter == null || letter.codePointCount(0, letter.length()) != 1 || !ArabicLetters.isLetter(letter.codePointAt(0))) {
            throw new IllegalArgumentException("A radical must be one Arabic letter.");
        }
        return letter;
    }

    private static ConjugatedCell cell(
            Person person,
            GrammaticalNumber number,
            FeatureGender gender,
            VerbAspect aspect,
            VerbMood mood,
            String surface) {
        return new ConjugatedCell(surface, new MorphologicalFeatures(person, number, gender, aspect, mood, VerbVoice.ACTIVE, null, null), FormOrigin.RULE_GENERATED);
    }

    public record ConjugatedCell(String surface, MorphologicalFeatures features, FormOrigin origin) {
    }
}
