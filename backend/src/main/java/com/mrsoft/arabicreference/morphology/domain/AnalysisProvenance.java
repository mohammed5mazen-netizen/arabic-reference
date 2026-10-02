package com.mrsoft.arabicreference.morphology.domain;

/**
 * Why a candidate exists. This is not a probability.
 */
public enum AnalysisProvenance {
    MANUAL_VERIFIED,
    EXACT_DICTIONARY,
    RULE_DERIVED,
    AMBIGUOUS
}
