package com.mrsoft.arabicreference.morphology.application;

import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import com.mrsoft.arabicreference.morphology.infrastructure.persistence.MorphologyAnalysisRepository;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Published morphology facts for other modules. Callers do not use morphology repositories.
 */
@Service
public class PublishedMorphologyQuery {

    private final MorphologyAnalysisRepository analyses;

    public PublishedMorphologyQuery(MorphologyAnalysisRepository analyses) {
        this.analyses = analyses;
    }

    @Transactional(readOnly = true)
    public Optional<Reading> reading(UUID id) {
        return analyses.findById(id)
                .filter(analysis -> analysis.getPublishedSnapshot() != null && analysis.getStatus() != PublicationStatus.ARCHIVED)
                .map(analysis -> new Reading(analysis.getId(), text(analysis.getPublishedSnapshot().get("patternOriginal"))));
    }

    @Transactional(readOnly = true)
    public boolean exists(UUID id) {
        return analyses.existsById(id);
    }

    private static String text(Object value) {
        return value == null ? null : value.toString();
    }

    public record Reading(UUID id, String patternOriginal) {
    }
}
