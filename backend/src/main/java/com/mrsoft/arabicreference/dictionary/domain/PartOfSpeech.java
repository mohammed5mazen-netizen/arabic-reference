package com.mrsoft.arabicreference.dictionary.domain;

/**
 * Lexicographic classes used by dictionary entries.
 * They are finer than the traditional اسم / فعل / حرف split, and they are not a grammar engine.
 */
public enum PartOfSpeech {
    NOUN,
    VERB,
    ADJECTIVE,
    ADVERB,
    PRONOUN,
    PREPOSITION,
    CONJUNCTION,
    PARTICLE,
    INTERJECTION,
    PROPER_NOUN,
    OTHER
}
