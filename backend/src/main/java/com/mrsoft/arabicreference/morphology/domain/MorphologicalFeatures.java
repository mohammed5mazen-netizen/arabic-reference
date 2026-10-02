package com.mrsoft.arabicreference.morphology.domain;

/**
 * Structured features. Null means the engine has no evidence, including case outside a syntactic context.
 * Aspect uses perfect, imperfect, and imperative rather than English tense names.
 * Mood names the Arabic إعراب of the imperfect: indicative, subjunctive, jussive.
 */
public record MorphologicalFeatures(
        Person person,
        GrammaticalNumber number,
        FeatureGender gender,
        VerbAspect aspect,
        VerbMood mood,
        VerbVoice voice,
        NominalCase grammaticalCase,
        Definiteness definiteness) {

    public static MorphologicalFeatures empty() {
        return new MorphologicalFeatures(null, null, null, null, null, null, null, null);
    }

    public enum Person {
        FIRST,
        SECOND,
        THIRD
    }

    public enum GrammaticalNumber {
        SINGULAR,
        DUAL,
        PLURAL
    }

    public enum FeatureGender {
        MASCULINE,
        FEMININE,
        NOT_APPLICABLE
    }

    public enum VerbAspect {
        PERFECT,
        IMPERFECT,
        IMPERATIVE
    }

    public enum VerbMood {
        INDICATIVE,
        SUBJUNCTIVE,
        JUSSIVE,
        NOT_APPLICABLE
    }

    public enum VerbVoice {
        ACTIVE,
        PASSIVE
    }

    public enum NominalCase {
        NOMINATIVE,
        ACCUSATIVE,
        GENITIVE
    }

    public enum Definiteness {
        DEFINITE,
        INDEFINITE,
        CONSTRUCT_STATE
    }
}
