package com.mrsoft.arabicreference.source.application;

import com.mrsoft.arabicreference.source.application.SourceViews.CitationView;
import com.mrsoft.arabicreference.shared.kernel.exception.ForbiddenOperationException;
import com.mrsoft.arabicreference.shared.kernel.exception.ResourceNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CitationChecks {

    private final SourceAdminService sources;

    public CitationChecks(SourceAdminService sources) {
        this.sources = sources;
    }

    public void requireExisting(UUID citationId) {
        if (citationId == null || sources.citations(List.of(citationId)).isEmpty()) {
            throw new ResourceNotFoundException("Citation was not found.");
        }
    }

    public List<CitationView> requirePublishable(Collection<UUID> citationIds) {
        List<UUID> distinct = new ArrayList<>(new LinkedHashSet<>(citationIds));
        distinct.removeIf(id -> id == null);
        if (distinct.isEmpty()) {
            throw new ForbiddenOperationException("Publication needs at least one citation.");
        }
        List<CitationView> views = sources.citations(distinct);
        if (views.size() != distinct.size()) {
            throw new ResourceNotFoundException("Citation was not found.");
        }
        for (CitationView citation : views) {
            if (!"PUBLISHED".equals(citation.sourceStatus()) || !citation.publishableLicense()) {
                throw new ForbiddenOperationException("A published record cannot cite a restricted, unknown, or unpublished source.");
            }
        }
        return views;
    }
}
