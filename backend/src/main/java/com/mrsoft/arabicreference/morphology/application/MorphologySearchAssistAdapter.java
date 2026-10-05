package com.mrsoft.arabicreference.morphology.application;

import com.mrsoft.arabicreference.morphology.domain.AnalysisCandidate;
import com.mrsoft.arabicreference.search.domain.MorphologySearchAssist;
import com.mrsoft.arabicreference.search.domain.SearchTuning;
import com.mrsoft.arabicreference.shared.kernel.exception.RateLimitedException;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class MorphologySearchAssistAdapter implements MorphologySearchAssist {

    private static final Logger log = LoggerFactory.getLogger(MorphologySearchAssistAdapter.class);

    private final MorphologyService morphology;

    public MorphologySearchAssistAdapter(MorphologyService morphology) {
        this.morphology = morphology;
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public List<UUID> entryIds(String word) {
        try {
            return morphology.analyze(word, "search").analyses().stream()
                    .map(AnalysisCandidate::lexicalEntryId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .limit(SearchTuning.MORPHOLOGY_CAP)
                    .toList();
        } catch (ValidationException | RateLimitedException | IllegalArgumentException exception) {
            log.debug("morphology assist skipped");
            return List.of();
        }
    }
}
