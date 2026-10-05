package com.mrsoft.arabicreference.literature.application;

import com.mrsoft.arabicreference.content.domain.KnowledgeTargetSource;
import com.mrsoft.arabicreference.content.domain.KnowledgeTargetType;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryFigureRepository;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryWorkRepository;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class LiteratureKnowledgeTarget implements KnowledgeTargetSource {

    private final LiteraryFigureRepository figures;
    private final LiteraryWorkRepository works;

    public LiteratureKnowledgeTarget(LiteraryFigureRepository figures, LiteraryWorkRepository works) {
        this.figures = figures;
        this.works = works;
    }

    @Override
    public boolean supports(KnowledgeTargetType type) {
        return type == KnowledgeTargetType.LITERARY_FIGURE || type == KnowledgeTargetType.LITERARY_WORK;
    }

    @Override
    public Optional<KnowledgeTarget> published(UUID id) {
        Optional<KnowledgeTarget> figure = figures.findById(id)
                .filter(item -> item.visibleToPublic())
                .map(item -> target(KnowledgeTargetType.LITERARY_FIGURE, id, item.getPublishedSnapshot(), "name", "/literature/figures/"));
        if (figure.isPresent()) {
            return figure;
        }
        return works.findById(id)
                .filter(item -> item.visibleToPublic())
                .map(item -> target(KnowledgeTargetType.LITERARY_WORK, id, item.getPublishedSnapshot(), "title", "/literature/works/"));
    }

    private static KnowledgeTarget target(KnowledgeTargetType type, UUID id, Map<String, Object> snapshot, String titleKey, String prefix) {
        String title = snapshot.get(titleKey) == null ? "" : String.valueOf(snapshot.get(titleKey));
        String slug = snapshot.get("slug") == null ? "" : String.valueOf(snapshot.get("slug"));
        return new KnowledgeTarget(type, id, title, prefix + slug);
    }
}
