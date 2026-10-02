package com.mrsoft.arabicreference.morphology.domain;

/**
 * Weakness classes. S3 conjugates only SOUND triliteral فَعَلَ when the lexical record says so.
 */
public enum VerbClass {
    SOUND,
    HAMZATED,
    ASSIMILATED,
    HOLLOW,
    DEFECTIVE,
    DOUBLED,
    LAFIF_MAFRUQ,
    LAFIF_MAQRUN
}
