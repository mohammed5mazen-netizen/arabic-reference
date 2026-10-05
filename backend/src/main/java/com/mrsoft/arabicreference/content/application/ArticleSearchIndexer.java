package com.mrsoft.arabicreference.content.application;

import com.mrsoft.arabicreference.content.infrastructure.persistence.ArticleEntity;
import com.mrsoft.arabicreference.content.infrastructure.persistence.ArticleRepository;
import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import com.mrsoft.arabicreference.search.domain.PublishedSearchSource;
import com.mrsoft.arabicreference.search.domain.SearchDocument;
import com.mrsoft.arabicreference.search.domain.SearchEntityType;
import com.mrsoft.arabicreference.search.domain.SearchIndex;
import com.mrsoft.arabicreference.search.domain.SearchText;
import com.mrsoft.arabicreference.search.domain.SearchTuning;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ArticleSearchIndexer implements PublishedSearchSource {

    private final ArticleRepository articles;
    private final SearchIndex index;

    public ArticleSearchIndexer(ArticleRepository articles, SearchIndex index) {
        this.articles = articles;
        this.index = index;
    }

    public void lock() {
        index.exclusiveLock();
    }

    public void onPublished(ArticleEntity article) {
        SearchDocument document = document(article);
        if (document != null) {
            index.upsert(document);
        }
    }

    public void onArchived(UUID id) {
        index.remove(SearchEntityType.ARTICLE, id);
    }

    @Override
    public List<SearchDocument> publishedDocuments() {
        List<SearchDocument> documents = new ArrayList<>();
        for (ArticleEntity article : articles.visibleToPublic(PublicationStatus.ARCHIVED)) {
            SearchDocument document = document(article);
            if (document != null) {
                documents.add(document);
            }
        }
        return documents;
    }

    private SearchDocument document(ArticleEntity article) {
        Map<String, Object> snapshot = article.getPublishedSnapshot();
        if (snapshot == null || article.getStatus() == PublicationStatus.ARCHIVED) {
            return null;
        }
        String title = text(snapshot.get("title"));
        if (title.isBlank()) {
            return null;
        }
        String excerpt = text(snapshot.get("excerpt"));
        List<String> parts = new ArrayList<>();
        parts.add(title);
        parts.add(excerpt);
        for (Map<String, Object> section : maps(snapshot.get("sections"))) {
            parts.add(text(section.get("heading")));
            parts.add(text(section.get("body")));
        }
        return new SearchDocument(
                SearchEntityType.ARTICLE,
                article.getId(),
                title,
                SearchText.normalized(title),
                SearchText.key(title),
                SearchText.blob(parts.toArray(String[]::new)),
                null,
                null,
                List.of(),
                null,
                "ARTICLE",
                null,
                SearchText.snippet(excerpt),
                "/articles/" + text(snapshot.get("slug")),
                article.getUpdatedAt(),
                0,
                0,
                0,
                SearchTuning.INDEX_VERSION);
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
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
