package com.mrsoft.arabicreference.literature.application;

import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryEraEntity;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryEraRepository;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryFigureEntity;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryFigureRepository;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryWorkEntity;
import com.mrsoft.arabicreference.literature.infrastructure.persistence.LiteraryWorkRepository;
import com.mrsoft.arabicreference.search.domain.PublishedSearchSource;
import com.mrsoft.arabicreference.search.domain.SearchDocument;
import com.mrsoft.arabicreference.search.domain.SearchEntityType;
import com.mrsoft.arabicreference.search.domain.SearchIndex;
import com.mrsoft.arabicreference.search.domain.SearchText;
import com.mrsoft.arabicreference.search.domain.SearchToken;
import com.mrsoft.arabicreference.search.domain.SearchTokenKind;
import com.mrsoft.arabicreference.search.domain.SearchTuning;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class LiteratureSearchIndexer implements PublishedSearchSource {

    private final LiteraryEraRepository eras;
    private final LiteraryFigureRepository figures;
    private final LiteraryWorkRepository works;
    private final SearchIndex index;

    public LiteratureSearchIndexer(
            LiteraryEraRepository eras,
            LiteraryFigureRepository figures,
            LiteraryWorkRepository works,
            SearchIndex index) {
        this.eras = eras;
        this.figures = figures;
        this.works = works;
        this.index = index;
    }

    public void lock() {
        index.exclusiveLock();
    }

    public void onEraPublished(LiteraryEraEntity era) {
        upsert(eraDocument(era));
    }

    public void onEraArchived(UUID id) {
        index.remove(SearchEntityType.LITERARY_ERA, id);
    }

    public void onFigurePublished(LiteraryFigureEntity figure) {
        upsert(figureDocument(figure));
    }

    public void onFigureArchived(UUID id) {
        index.remove(SearchEntityType.LITERARY_FIGURE, id);
    }

    public void onWorkPublished(LiteraryWorkEntity work) {
        upsert(workDocument(work));
    }

    public void onWorkArchived(UUID id) {
        index.remove(SearchEntityType.LITERARY_WORK, id);
    }

    @Override
    public List<SearchDocument> publishedDocuments() {
        List<SearchDocument> documents = new ArrayList<>();
        for (LiteraryEraEntity era : eras.visibleToPublic(PublicationStatus.ARCHIVED)) {
            add(documents, eraDocument(era));
        }
        for (LiteraryFigureEntity figure : figures.visibleToPublic(PublicationStatus.ARCHIVED)) {
            add(documents, figureDocument(figure));
        }
        for (LiteraryWorkEntity work : works.visibleToPublic(PublicationStatus.ARCHIVED)) {
            add(documents, workDocument(work));
        }
        return documents;
    }

    private void upsert(SearchDocument document) {
        if (document != null) {
            index.upsert(document);
        }
    }

    private static void add(List<SearchDocument> documents, SearchDocument document) {
        if (document != null) {
            documents.add(document);
        }
    }

    private SearchDocument eraDocument(LiteraryEraEntity era) {
        Map<String, Object> snapshot = era.getPublishedSnapshot();
        if (snapshot == null || era.getStatus() == PublicationStatus.ARCHIVED) {
            return null;
        }
        String title = text(snapshot.get("name"));
        String summary = text(snapshot.get("summary"));
        String searchable = SearchText.blob(title, summary, text(snapshot.get("historicalContext")), text(snapshot.get("startDescription")), text(snapshot.get("endDescription")));
        return document(SearchEntityType.LITERARY_ERA, era.getId(), title, searchable, List.of(), SearchText.snippet(summary), "/literature/eras/" + text(snapshot.get("slug")), era.getUpdatedAt());
    }

    private SearchDocument figureDocument(LiteraryFigureEntity figure) {
        Map<String, Object> snapshot = figure.getPublishedSnapshot();
        if (snapshot == null || figure.getStatus() == PublicationStatus.ARCHIVED) {
            return null;
        }
        String title = text(snapshot.get("name"));
        String biography = text(snapshot.get("biography"));
        List<SearchToken> tokens = new ArrayList<>();
        List<String> parts = new ArrayList<>();
        parts.add(title);
        parts.add(biography);
        for (Map<String, Object> alias : maps(snapshot.get("aliases"))) {
            String original = text(alias.get("alias"));
            if (original.isBlank()) {
                continue;
            }
            tokens.add(new SearchToken(original, SearchText.key(original), SearchTokenKind.ALIAS));
            parts.add(original);
        }
        return document(SearchEntityType.LITERARY_FIGURE, figure.getId(), title, SearchText.blob(parts.toArray(String[]::new)), tokens, SearchText.snippet(biography), "/literature/figures/" + text(snapshot.get("slug")), figure.getUpdatedAt());
    }

    private SearchDocument workDocument(LiteraryWorkEntity work) {
        Map<String, Object> snapshot = work.getPublishedSnapshot();
        if (snapshot == null || work.getStatus() == PublicationStatus.ARCHIVED) {
            return null;
        }
        String title = text(snapshot.get("title"));
        String description = text(snapshot.get("description"));
        List<SearchToken> tokens = new ArrayList<>();
        List<String> parts = new ArrayList<>();
        parts.add(title);
        parts.add(description);
        for (String alias : strings(snapshot.get("aliases"))) {
            if (alias.isBlank()) {
                continue;
            }
            tokens.add(new SearchToken(alias, SearchText.key(alias), SearchTokenKind.ALIAS));
            parts.add(alias);
        }
        for (Map<String, Object> excerpt : maps(snapshot.get("excerpts"))) {
            parts.add(text(excerpt.get("text")));
        }
        return document(SearchEntityType.LITERARY_WORK, work.getId(), title, SearchText.blob(parts.toArray(String[]::new)), tokens, SearchText.snippet(description), "/literature/works/" + text(snapshot.get("slug")), work.getUpdatedAt());
    }

    private SearchDocument document(
            SearchEntityType type,
            UUID id,
            String title,
            String searchable,
            List<SearchToken> tokens,
            String snippet,
            String url,
            Instant publishedAt) {
        if (title.isBlank()) {
            return null;
        }
        return new SearchDocument(type, id, title, SearchText.normalized(title), SearchText.key(title), searchable == null ? "" : searchable, null, null, tokens, null, "LITERATURE", null, snippet == null ? "" : snippet, url, publishedAt, 0, 0, 0, SearchTuning.INDEX_VERSION);
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static List<String> strings(Object value) {
        if (!(value instanceof List<?> items)) {
            return List.of();
        }
        List<String> strings = new ArrayList<>();
        for (Object item : items) {
            if (item != null) {
                strings.add(String.valueOf(item));
            }
        }
        return strings;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> maps(Object value) {
        if (!(value instanceof List<?> items)) {
            return List.of();
        }
        List<Map<String, Object>> maps = new ArrayList<>();
        for (Object item : items) {
            if (item instanceof Map<?, ?> raw) {
                maps.add((Map<String, Object>) raw);
            }
        }
        return maps;
    }
}
