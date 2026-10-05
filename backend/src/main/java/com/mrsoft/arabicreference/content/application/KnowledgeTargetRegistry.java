package com.mrsoft.arabicreference.content.application;

import com.mrsoft.arabicreference.content.domain.KnowledgeTargetSource;
import com.mrsoft.arabicreference.content.domain.KnowledgeTargetSource.KnowledgeTarget;
import com.mrsoft.arabicreference.content.domain.KnowledgeTargetType;
import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import com.mrsoft.arabicreference.shared.kernel.exception.ResourceNotFoundException;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class KnowledgeTargetRegistry {

    private final List<KnowledgeTargetSource> sources;

    public KnowledgeTargetRegistry(List<KnowledgeTargetSource> sources) {
        this.sources = sources;
    }

    public KnowledgeTarget requirePublished(KnowledgeTargetType type, UUID id) {
        if (type == null) {
            throw invalid("targetType", "Choose a related record.");
        }
        if (id == null) {
            throw invalid("targetId", "Choose a related record.");
        }
        if (sources.stream().noneMatch(source -> source.supports(type))) {
            throw invalid("targetType", "This relation target is not supported.");
        }
        return findPublished(type, id).orElseThrow(() -> new ResourceNotFoundException("The related record is not published."));
    }

    public Optional<KnowledgeTarget> findPublished(KnowledgeTargetType type, UUID id) {
        if (type == null || id == null) {
            return Optional.empty();
        }
        for (KnowledgeTargetSource source : sources) {
            if (!source.supports(type)) {
                continue;
            }
            Optional<KnowledgeTarget> found = source.published(id).filter(target -> target.type() == type);
            if (found.isPresent()) {
                return found;
            }
        }
        return Optional.empty();
    }

    private static ValidationException invalid(String field, String message) {
        return new ValidationException(message, List.of(new FieldErrorDetail(field, message)));
    }
}
