package com.mrsoft.arabicreference.content.application;

import com.mrsoft.arabicreference.content.application.ArticleViews.ArticleAdmin;
import com.mrsoft.arabicreference.content.application.ArticleViews.ArticleDraft;
import com.mrsoft.arabicreference.content.application.ArticleViews.ArticleSummary;
import com.mrsoft.arabicreference.content.application.ArticleViews.ArticleUpdate;
import com.mrsoft.arabicreference.content.application.ArticleViews.CitationDraft;
import com.mrsoft.arabicreference.content.application.ArticleViews.CitationView;
import com.mrsoft.arabicreference.content.application.ArticleViews.PageResult;
import com.mrsoft.arabicreference.content.application.ArticleViews.RelationDraft;
import com.mrsoft.arabicreference.content.application.ArticleViews.RelationView;
import com.mrsoft.arabicreference.content.application.ArticleViews.ReviewItem;
import com.mrsoft.arabicreference.content.application.ArticleViews.SectionDraft;
import com.mrsoft.arabicreference.content.application.ArticleViews.SectionView;
import com.mrsoft.arabicreference.content.application.ArticleViews.TagDraft;
import com.mrsoft.arabicreference.content.application.ArticleViews.TagView;
import com.mrsoft.arabicreference.content.domain.KnowledgeOwnerType;
import com.mrsoft.arabicreference.content.domain.KnowledgeTargetSource.KnowledgeTarget;
import com.mrsoft.arabicreference.content.infrastructure.persistence.ArticleCitationEntity;
import com.mrsoft.arabicreference.content.infrastructure.persistence.ArticleCitationRepository;
import com.mrsoft.arabicreference.content.infrastructure.persistence.ArticleEntity;
import com.mrsoft.arabicreference.content.infrastructure.persistence.ArticleRepository;
import com.mrsoft.arabicreference.content.infrastructure.persistence.ArticleSectionEntity;
import com.mrsoft.arabicreference.content.infrastructure.persistence.ArticleSectionRepository;
import com.mrsoft.arabicreference.content.infrastructure.persistence.ArticleTagEntity;
import com.mrsoft.arabicreference.content.infrastructure.persistence.ArticleTagLinkEntity;
import com.mrsoft.arabicreference.content.infrastructure.persistence.ArticleTagLinkRepository;
import com.mrsoft.arabicreference.content.infrastructure.persistence.ArticleTagRepository;
import com.mrsoft.arabicreference.content.infrastructure.persistence.KnowledgeRelationEntity;
import com.mrsoft.arabicreference.content.infrastructure.persistence.KnowledgeRelationRepository;
import com.mrsoft.arabicreference.identity.application.AuthorizationService;
import com.mrsoft.arabicreference.identity.domain.AuditEventType;
import com.mrsoft.arabicreference.identity.domain.PermissionCatalog;
import com.mrsoft.arabicreference.linguistics.application.EditorialStore;
import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import com.mrsoft.arabicreference.linguistics.domain.text.ContentSlugs;
import com.mrsoft.arabicreference.linguistics.domain.text.KnowledgeText;
import com.mrsoft.arabicreference.shared.kernel.exception.ConflictException;
import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import com.mrsoft.arabicreference.shared.kernel.exception.ResourceNotFoundException;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import com.mrsoft.arabicreference.shared.kernel.id.Ids;
import com.mrsoft.arabicreference.source.application.CitationChecks;
import com.mrsoft.arabicreference.source.application.SourceLines;
import com.mrsoft.arabicreference.source.application.SourceViews;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArticleAdminService {

    static final String ARTICLE = "article";

    private final ArticleRepository articles;
    private final ArticleSectionRepository sections;
    private final ArticleTagRepository tags;
    private final ArticleTagLinkRepository tagLinks;
    private final ArticleCitationRepository articleCitations;
    private final KnowledgeRelationRepository relations;
    private final EditorialStore editorial;
    private final AuthorizationService authorization;
    private final CitationChecks citations;
    private final KnowledgeTargetRegistry targets;
    private final ArticleSearchIndexer search;

    public ArticleAdminService(
            ArticleRepository articles,
            ArticleSectionRepository sections,
            ArticleTagRepository tags,
            ArticleTagLinkRepository tagLinks,
            ArticleCitationRepository articleCitations,
            KnowledgeRelationRepository relations,
            EditorialStore editorial,
            AuthorizationService authorization,
            CitationChecks citations,
            KnowledgeTargetRegistry targets,
            ArticleSearchIndexer search) {
        this.articles = articles;
        this.sections = sections;
        this.tags = tags;
        this.tagLinks = tagLinks;
        this.articleCitations = articleCitations;
        this.relations = relations;
        this.editorial = editorial;
        this.authorization = authorization;
        this.citations = citations;
        this.targets = targets;
        this.search = search;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.ARTICLE_VIEW + "')")
    public PageResult<ArticleSummary> articles(int page, int size) {
        var result = articles.findAllByOrderByUpdatedAtDesc(page(page, size));
        return new PageResult<>(result.map(article -> new ArticleSummary(article.getId(), article.getTitleOriginal(), article.getSlug(), article.getArticleType().name(), article.getStatus().name(), article.getVersion())).toList(), page, size, result.getTotalElements());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.ARTICLE_VIEW + "')")
    public ArticleAdmin article(UUID id) {
        return articleAdmin(articles.findById(id).orElseThrow(() -> missing("Article")));
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.ARTICLE_CREATE + "')")
    public ArticleAdmin create(ArticleDraft draft) {
        UUID actor = authorization.requireAccess().userId();
        ArticleEntity article = new ArticleEntity();
        editorial.prepareNew(article, actor);
        apply(article, draft.title(), draft.excerpt(), draft.articleType(), draft.coverLabel(), draft.editorName());
        article.setSlug(ContentSlugs.of(article.getTitleNormalized(), article.getId()));
        editorial.persist(articles, article, actor, AuditEventType.ARTICLE_CREATED, ARTICLE);
        return articleAdmin(article);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.ARTICLE_EDIT + "')")
    public ArticleAdmin update(UUID id, ArticleUpdate update) {
        ArticleEntity article = locked(id, update.version());
        UUID actor = authorization.requireAccess().userId();
        editorial.open(article, actor, "article update", ARTICLE);
        apply(article, update.title(), update.excerpt(), update.articleType(), update.coverLabel(), update.editorName());
        editorial.persist(articles, article, actor, AuditEventType.ARTICLE_UPDATED, ARTICLE);
        return articleAdmin(article);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.ARTICLE_EDIT + "')")
    public ArticleAdmin addSection(UUID id, SectionDraft draft) {
        String heading = KnowledgeText.required(draft.heading(), "heading", 200);
        String body = KnowledgeText.required(draft.body(), "body", 8000);
        ArticleEntity article = locked(id, draft.version());
        UUID actor = authorization.requireAccess().userId();
        editorial.open(article, actor, "section added", ARTICLE);
        ArticleSectionEntity section = new ArticleSectionEntity();
        section.setId(Ids.random());
        section.setArticleId(id);
        section.setHeading(heading);
        section.setBody(body);
        section.setDisplayOrder(sections.findByArticleIdOrderByDisplayOrderAsc(id).size());
        sections.save(section);
        editorial.persist(articles, article, actor, AuditEventType.ARTICLE_UPDATED, ARTICLE);
        return articleAdmin(article);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.ARTICLE_EDIT + "')")
    public ArticleAdmin addTag(UUID id, TagDraft draft) {
        String name = KnowledgeText.required(draft.name(), "name", 80);
        String normalized = KnowledgeText.normalized(name);
        ArticleEntity article = locked(id, draft.version());
        ArticleTagEntity tag = tags.findByNormalized(normalized).orElseGet(() -> {
            ArticleTagEntity created = new ArticleTagEntity();
            created.setId(Ids.random());
            created.setName(name);
            created.setNormalized(normalized);
            return tags.save(created);
        });
        if (tagLinks.existsByArticleIdAndTagId(id, tag.getId())) {
            throw new ConflictException("This article already has that tag.");
        }
        UUID actor = authorization.requireAccess().userId();
        editorial.open(article, actor, "tag added", ARTICLE);
        tagLinks.save(new ArticleTagLinkEntity(id, tag.getId()));
        editorial.persist(articles, article, actor, AuditEventType.ARTICLE_UPDATED, ARTICLE);
        return articleAdmin(article);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.CITATION_MANAGE + "')")
    public ArticleAdmin cite(UUID id, CitationDraft draft) {
        if (draft.citationId() == null) {
            throw invalid("citationId", "Choose a citation.");
        }
        citations.requireExisting(draft.citationId());
        if (draft.sectionId() != null && sections.findByIdAndArticleId(draft.sectionId(), id).isEmpty()) {
            throw missing("Section");
        }
        ArticleEntity article = locked(id, draft.version());
        UUID actor = authorization.requireAccess().userId();
        editorial.open(article, actor, "citation linked", ARTICLE);
        boolean exists = draft.sectionId() == null
                ? articleCitations.existsByArticleIdAndSectionIdIsNullAndCitationId(id, draft.citationId())
                : articleCitations.existsByArticleIdAndSectionIdAndCitationId(id, draft.sectionId(), draft.citationId());
        if (!exists) {
            ArticleCitationEntity row = new ArticleCitationEntity();
            row.setId(Ids.random());
            row.setArticleId(id);
            row.setSectionId(draft.sectionId());
            row.setCitationId(draft.citationId());
            articleCitations.save(row);
        }
        editorial.persist(articles, article, actor, AuditEventType.CITATION_ADDED, ARTICLE);
        return articleAdmin(article);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.ARTICLE_EDIT + "')")
    public ArticleAdmin addRelation(UUID id, RelationDraft draft) {
        KnowledgeTarget target = targets.requirePublished(draft.targetType(), draft.targetId());
        ArticleEntity article = locked(id, draft.version());
        if (relations.existsByOwnerTypeAndOwnerIdAndTargetTypeAndTargetId(KnowledgeOwnerType.ARTICLE, id, target.type(), target.id())) {
            throw new ConflictException("This article already links to that record.");
        }
        UUID actor = authorization.requireAccess().userId();
        editorial.open(article, actor, "relation added", ARTICLE);
        KnowledgeRelationEntity relation = new KnowledgeRelationEntity();
        relation.setId(Ids.random());
        relation.setOwnerType(KnowledgeOwnerType.ARTICLE);
        relation.setOwnerId(id);
        relation.setTargetType(target.type());
        relation.setTargetId(target.id());
        relations.save(relation);
        editorial.persist(articles, article, actor, AuditEventType.ARTICLE_UPDATED, ARTICLE);
        return articleAdmin(article);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.ARTICLE_EDIT + "')")
    public ArticleAdmin submit(UUID id, long version) {
        ArticleEntity article = locked(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.submit(article);
        editorial.persist(articles, article, actor, AuditEventType.ARTICLE_SUBMITTED, ARTICLE);
        return articleAdmin(article);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.ARTICLE_REVIEW + "')")
    public ArticleAdmin verify(UUID id, long version) {
        ArticleEntity article = locked(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.review(article, actor);
        editorial.persist(articles, article, actor, AuditEventType.ARTICLE_VERIFIED, ARTICLE);
        return articleAdmin(article);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.ARTICLE_REVIEW + "')")
    public ArticleAdmin requestChanges(UUID id, long version, String reason) {
        ArticleEntity article = locked(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.requestChanges(article, actor, KnowledgeText.required(reason, "reason", 500));
        editorial.persist(articles, article, actor, AuditEventType.ARTICLE_CHANGES_REQUESTED, ARTICLE);
        return articleAdmin(article);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.ARTICLE_PUBLISH + "')")
    public ArticleAdmin publish(UUID id, long version) {
        search.lock();
        ArticleEntity article = locked(id, version);
        UUID actor = authorization.requireAccess().userId();
        List<ArticleSectionEntity> parts = sections.findByArticleIdOrderByDisplayOrderAsc(id);
        if (parts.isEmpty()) {
            throw new ConflictException("A published article needs at least one section.");
        }
        List<ArticleCitationEntity> cites = articleCitations.findByArticleId(id);
        List<SourceViews.CitationView> views = citations.requirePublishable(cites.stream().map(ArticleCitationEntity::getCitationId).toList());
        List<KnowledgeRelationEntity> links = new ArrayList<>();
        List<KnowledgeTarget> resolved = new ArrayList<>();
        for (KnowledgeRelationEntity link : relations.findByOwnerTypeAndOwnerId(KnowledgeOwnerType.ARTICLE, id)) {
            targets.findPublished(link.getTargetType(), link.getTargetId()).ifPresent(target -> {
                links.add(link);
                resolved.add(target);
            });
        }
        editorial.publish(article, actor, "ARTICLE");
        article.setPublishedSnapshot(snapshot(article, parts, cites, views, links, resolved));
        editorial.persist(articles, article, actor, AuditEventType.ARTICLE_PUBLISHED, ARTICLE);
        search.onPublished(article);
        return articleAdmin(article);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.ARTICLE_PUBLISH + "')")
    public ArticleAdmin archive(UUID id, long version) {
        search.lock();
        ArticleEntity article = locked(id, version);
        UUID actor = authorization.requireAccess().userId();
        editorial.archive(article);
        editorial.persist(articles, article, actor, AuditEventType.ARTICLE_ARCHIVED, ARTICLE);
        search.onArchived(id);
        return articleAdmin(article);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.ARTICLE_REVIEW + "')")
    public List<ReviewItem> reviewQueue() {
        List<ReviewItem> items = new ArrayList<>();
        for (ArticleEntity article : articles.findByStatusOrderByUpdatedAtDesc(PublicationStatus.IN_REVIEW)) {
            items.add(new ReviewItem("ARTICLE", article.getId(), article.getTitleOriginal(), article.getStatus().name(), article.getVersion()));
        }
        return items;
    }

    private Map<String, Object> snapshot(
            ArticleEntity article,
            List<ArticleSectionEntity> parts,
            List<ArticleCitationEntity> cites,
            List<SourceViews.CitationView> views,
            List<KnowledgeRelationEntity> links,
            List<KnowledgeTarget> resolved) {
        Map<UUID, SourceViews.CitationView> byId = new LinkedHashMap<>();
        for (SourceViews.CitationView view : views) {
            byId.putIfAbsent(view.id(), view);
        }
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("title", article.getTitleOriginal());
        snapshot.put("slug", article.getSlug());
        snapshot.put("excerpt", article.getExcerpt());
        snapshot.put("articleType", article.getArticleType().name());
        snapshot.put("coverLabel", article.getCoverLabel());
        snapshot.put("editorName", article.getEditorName());
        List<Map<String, Object>> sectionMaps = new ArrayList<>();
        for (ArticleSectionEntity section : parts) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("heading", section.getHeading());
            map.put("body", section.getBody());
            map.put("sources", SourceLines.of(viewsFor(cites, section.getId(), byId)));
            sectionMaps.add(map);
        }
        snapshot.put("sections", sectionMaps);
        snapshot.put("tags", tagLinks.findByArticleId(article.getId()).stream()
                .map(link -> tags.findById(link.getTagId()).map(ArticleTagEntity::getName).orElse(""))
                .filter(name -> !name.isBlank())
                .toList());
        List<Map<String, Object>> relationMaps = new ArrayList<>();
        for (int index = 0; index < links.size(); index++) {
            KnowledgeTarget target = resolved.get(index);
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("targetType", target.type().name());
            map.put("title", target.title());
            map.put("url", target.url());
            relationMaps.add(map);
        }
        snapshot.put("relations", relationMaps);
        snapshot.put("sources", SourceLines.of(viewsFor(cites, null, byId)));
        return snapshot;
    }

    private static List<SourceViews.CitationView> viewsFor(List<ArticleCitationEntity> cites, UUID sectionId, Map<UUID, SourceViews.CitationView> byId) {
        List<SourceViews.CitationView> selected = new ArrayList<>();
        for (ArticleCitationEntity cite : cites) {
            boolean sameSection = sectionId == null ? cite.getSectionId() == null : sectionId.equals(cite.getSectionId());
            if (!sameSection) {
                continue;
            }
            SourceViews.CitationView view = byId.get(cite.getCitationId());
            if (view != null) {
                selected.add(view);
            }
        }
        return selected;
    }

    private void apply(ArticleEntity article, String title, String excerpt, com.mrsoft.arabicreference.content.domain.ArticleType articleType, String coverLabel, String editorName) {
        if (articleType == null) {
            throw invalid("articleType", "Choose an article type.");
        }
        String original = KnowledgeText.required(title, "title", 200);
        article.setTitleOriginal(original);
        article.setTitleNormalized(KnowledgeText.normalized(original));
        article.setExcerpt(KnowledgeText.required(excerpt, "excerpt", 500));
        article.setArticleType(articleType);
        article.setCoverLabel(KnowledgeText.optional(coverLabel, "coverLabel", 160));
        article.setEditorName(KnowledgeText.plain(editorName, "editorName", 160));
    }

    private ArticleEntity locked(UUID id, long version) {
        ArticleEntity article = articles.lockById(id).orElseThrow(() -> missing("Article"));
        editorial.requireVersion(article, version);
        return article;
    }

    private ArticleAdmin articleAdmin(ArticleEntity article) {
        List<SectionView> sectionViews = sections.findByArticleIdOrderByDisplayOrderAsc(article.getId()).stream()
                .map(section -> new SectionView(section.getId(), section.getHeading(), section.getBody(), section.getDisplayOrder()))
                .toList();
        List<TagView> tagViews = new ArrayList<>();
        for (ArticleTagLinkEntity link : tagLinks.findByArticleId(article.getId())) {
            tags.findById(link.getTagId()).ifPresent(tag -> tagViews.add(new TagView(tag.getId(), tag.getName())));
        }
        List<CitationView> citationViews = articleCitations.findByArticleId(article.getId()).stream()
                .map(cite -> new CitationView(cite.getId(), cite.getSectionId(), cite.getCitationId()))
                .toList();
        List<RelationView> relationViews = relations.findByOwnerTypeAndOwnerId(KnowledgeOwnerType.ARTICLE, article.getId()).stream()
                .map(relation -> new RelationView(relation.getId(), relation.getTargetType().name(), relation.getTargetId()))
                .toList();
        return new ArticleAdmin(article.getId(), article.getTitleOriginal(), article.getSlug(), article.getExcerpt(), article.getArticleType().name(), article.getCoverLabel(), article.getEditorName(), article.getStatus().name(), article.getVersion(), sectionViews, tagViews, citationViews, relationViews);
    }

    private static Pageable page(int page, int size) {
        if (page < 0 || size < 1 || size > 50) {
            throw invalid("size", "Page size must be from 1 to 50.");
        }
        return PageRequest.of(page, size);
    }

    private static ResourceNotFoundException missing(String name) {
        return new ResourceNotFoundException(name + " was not found.");
    }

    private static ValidationException invalid(String field, String message) {
        return new ValidationException(message, List.of(new FieldErrorDetail(field, message)));
    }
}
