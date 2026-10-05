package com.mrsoft.arabicreference.content.application;

import com.mrsoft.arabicreference.content.domain.ArticleType;
import com.mrsoft.arabicreference.content.domain.KnowledgeTargetType;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ArticleViews {

    private ArticleViews() {
    }

    public record PageResult<T>(List<T> items, int page, int size, long total) {
    }

    public record ArticleDraft(String title, String excerpt, ArticleType articleType, String coverLabel, String editorName) {
    }

    public record ArticleUpdate(long version, String title, String excerpt, ArticleType articleType, String coverLabel, String editorName) {
    }

    public record SectionDraft(long version, String heading, String body) {
    }

    public record TagDraft(long version, String name) {
    }

    public record CitationDraft(long version, UUID citationId, UUID sectionId) {
    }

    public record RelationDraft(long version, KnowledgeTargetType targetType, UUID targetId) {
    }

    public record VersionRequest(long version) {
    }

    public record ReasonRequest(long version, String reason) {
    }

    public record ArticleSummary(UUID id, String title, String slug, String articleType, String status, long version) {
    }

    public record SectionView(UUID id, String heading, String body, int displayOrder) {
    }

    public record TagView(UUID id, String name) {
    }

    public record CitationView(UUID id, UUID sectionId, UUID citationId) {
    }

    public record RelationView(UUID id, String targetType, UUID targetId) {
    }

    public record ArticleAdmin(
            UUID id,
            String title,
            String slug,
            String excerpt,
            String articleType,
            String coverLabel,
            String editorName,
            String status,
            long version,
            List<SectionView> sections,
            List<TagView> tags,
            List<CitationView> citations,
            List<RelationView> relations) {
    }

    public record ReviewItem(String kind, UUID id, String title, String status, long version) {
    }

    public record PublicArticleLink(String title, String slug, String excerpt, String articleType, String coverLabel) {
    }

    public record PublicArticle(
            String title,
            String slug,
            String excerpt,
            String articleType,
            String coverLabel,
            String editorName,
            List<Map<String, Object>> sections,
            List<String> tags,
            List<Map<String, Object>> relations,
            List<Map<String, Object>> sources) {
    }
}
