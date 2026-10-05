package com.mrsoft.arabicreference.search.application;

import com.mrsoft.arabicreference.identity.application.AuditRecorder;
import com.mrsoft.arabicreference.identity.application.AuthorizationService;
import com.mrsoft.arabicreference.identity.domain.AuditEventType;
import com.mrsoft.arabicreference.identity.domain.PermissionCatalog;
import com.mrsoft.arabicreference.search.application.SearchViews.SearchStatusView;
import com.mrsoft.arabicreference.search.domain.PublishedSearchSource;
import com.mrsoft.arabicreference.search.domain.SearchDocument;
import com.mrsoft.arabicreference.search.domain.SearchEntityType;
import com.mrsoft.arabicreference.search.domain.SearchIndex;
import com.mrsoft.arabicreference.search.domain.SearchIndex.StoredDocument;
import com.mrsoft.arabicreference.search.domain.SearchTuning;
import com.mrsoft.arabicreference.shared.kernel.exception.ConflictException;
import com.mrsoft.arabicreference.shared.kernel.security.AuthenticatedAccess;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SearchAdminService {

    private final SearchIndex index;
    private final List<PublishedSearchSource> sources;
    private final AuthorizationService authorization;
    private final AuditRecorder audit;

    public SearchAdminService(
            SearchIndex index,
            List<PublishedSearchSource> sources,
            AuthorizationService authorization,
            AuditRecorder audit) {
        this.index = index;
        this.sources = sources;
        this.authorization = authorization;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.SEARCH_ADMIN_VIEW + "')")
    public SearchStatusView status() {
        return view(compare(loadPublished()));
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.SEARCH_REINDEX + "')")
    public SearchStatusView rebuild() {
        AuthenticatedAccess actor = authorization.requireAccess();
        if (!index.tryExclusiveLock()) {
            throw new ConflictException("A search rebuild is already running.");
        }
        List<SearchDocument> documents = loadPublished();
        index.replaceAll(documents, actor.userId());
        audit.record(actor.userId(), AuditEventType.SEARCH_INDEX_REBUILT, "search_index", "published", Map.of("documents", Integer.toString(documents.size())));
        return view(new Difference(0, 0, 0));
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.SEARCH_REINDEX + "')")
    public SearchStatusView repair() {
        AuthenticatedAccess actor = authorization.requireAccess();
        if (!index.tryExclusiveLock()) {
            throw new ConflictException("A search rebuild is already running.");
        }
        List<SearchDocument> documents = loadPublished();
        Difference difference = compare(documents);
        if (difference.consistent()) {
            return view(difference);
        }
        index.replaceAll(documents, actor.userId());
        audit.record(actor.userId(), AuditEventType.SEARCH_INDEX_REPAIR, "search_index", "published", Map.of(
                "missing", Integer.toString(difference.missing()),
                "stale", Integer.toString(difference.stale()),
                "outdated", Integer.toString(difference.outdated())));
        return view(new Difference(0, 0, 0));
    }

    private List<SearchDocument> loadPublished() {
        return sources.stream().flatMap(source -> source.publishedDocuments().stream()).toList();
    }

    private Difference compare(List<SearchDocument> documents) {
        Set<String> expected = new HashSet<>();
        for (SearchDocument document : documents) {
            expected.add(key(document.entityType(), document.entityId().toString()));
        }
        Set<String> stored = new HashSet<>();
        int outdated = 0;
        for (StoredDocument document : index.storedDocuments()) {
            String key = key(document.type(), document.entityId().toString());
            stored.add(key);
            if (document.indexVersion() != SearchTuning.INDEX_VERSION) {
                outdated++;
            }
        }
        int missing = 0;
        for (String key : expected) {
            if (!stored.contains(key)) {
                missing++;
            }
        }
        int stale = 0;
        for (String key : stored) {
            if (!expected.contains(key)) {
                stale++;
            }
        }
        return new Difference(missing, stale, outdated);
    }

    private SearchStatusView view(Difference difference) {
        SearchIndex.SearchIndexState state = index.state();
        Map<SearchEntityType, Long> counts = new EnumMap<>(SearchEntityType.class);
        for (SearchEntityType type : SearchEntityType.values()) {
            counts.put(type, 0L);
        }
        for (StoredDocument document : index.storedDocuments()) {
            counts.merge(document.type(), 1L, Long::sum);
        }
        Map<String, Long> named = new LinkedHashMap<>();
        counts.forEach((type, count) -> named.put(type.name(), count));
        return new SearchStatusView(
                state.indexVersion(),
                state.documentCount(),
                state.lastRebuildAt() == null ? null : state.lastRebuildAt().toString(),
                named,
                difference.consistent(),
                difference.missing(),
                difference.stale(),
                difference.outdated());
    }

    private static String key(SearchEntityType type, String id) {
        return type.name() + ":" + id;
    }

    private record Difference(int missing, int stale, int outdated) {
        private boolean consistent() {
            return missing == 0 && stale == 0 && outdated == 0;
        }
    }
}
