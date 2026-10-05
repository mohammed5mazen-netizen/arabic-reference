package com.mrsoft.arabicreference.content.application;

import com.mrsoft.arabicreference.content.application.ArticleViews.PublicArticle;
import com.mrsoft.arabicreference.content.application.ArticleViews.PublicArticleLink;
import com.mrsoft.arabicreference.content.domain.KnowledgeTargetType;
import com.mrsoft.arabicreference.content.infrastructure.persistence.ArticleRepository;
import com.mrsoft.arabicreference.content.infrastructure.persistence.KnowledgeRelationRepository;
import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import com.mrsoft.arabicreference.shared.kernel.exception.ResourceNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArticleQueryService {

    private final ArticleRepository articles;
    private final KnowledgeRelationRepository relations;

    public ArticleQueryService(ArticleRepository articles, KnowledgeRelationRepository relations) {
        this.articles = articles;
        this.relations = relations;
    }

    @Transactional(readOnly = true)
    public List<PublicArticleLink> articles() {
        List<PublicArticleLink> links = new ArrayList<>();
        for (var article : articles.visibleToPublic(PublicationStatus.ARCHIVED)) {
            Map<String, Object> snapshot = article.getPublishedSnapshot();
            links.add(new PublicArticleLink(text(snapshot.get("title")), text(snapshot.get("slug")), text(snapshot.get("excerpt")), text(snapshot.get("articleType")), text(snapshot.get("coverLabel"))));
        }
        return links;
    }

    @Transactional(readOnly = true)
    public PublicArticle article(String slug) {
        var article = articles.findBySlug(slug).filter(item -> item.visibleToPublic()).orElseThrow(() -> new ResourceNotFoundException("Article was not found."));
        Map<String, Object> snapshot = article.getPublishedSnapshot();
        return new PublicArticle(
                text(snapshot.get("title")),
                text(snapshot.get("slug")),
                text(snapshot.get("excerpt")),
                text(snapshot.get("articleType")),
                text(snapshot.get("coverLabel")),
                text(snapshot.get("editorName")),
                maps(snapshot.get("sections")),
                strings(snapshot.get("tags")),
                maps(snapshot.get("relations")),
                maps(snapshot.get("sources")));
    }

    @Transactional(readOnly = true)
    public List<PublicArticleLink> relatedTo(KnowledgeTargetType type, UUID targetId) {
        List<PublicArticleLink> links = new ArrayList<>();
        for (var relation : relations.findByTargetTypeAndTargetId(type, targetId)) {
            if (links.size() >= 8) {
                break;
            }
            var article = articles.findById(relation.getOwnerId()).orElse(null);
            if (article == null || !article.visibleToPublic()) {
                continue;
            }
            Map<String, Object> snapshot = article.getPublishedSnapshot();
            links.add(new PublicArticleLink(text(snapshot.get("title")), text(snapshot.get("slug")), text(snapshot.get("excerpt")), text(snapshot.get("articleType")), text(snapshot.get("coverLabel"))));
        }
        return links;
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static List<String> strings(Object value) {
        if (!(value instanceof List<?> items)) {
            return List.of();
        }
        List<String> strings = new ArrayList<>();
        for (Object item : items) {
            if (item != null && !(item instanceof Map<?, ?>)) {
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
