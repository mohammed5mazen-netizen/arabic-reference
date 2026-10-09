package com.mrsoft.arabicreference.source.application;

import com.mrsoft.arabicreference.identity.application.AuditRecorder;
import com.mrsoft.arabicreference.identity.application.AuthorizationService;
import com.mrsoft.arabicreference.identity.domain.AuditEventType;
import com.mrsoft.arabicreference.identity.domain.PermissionCatalog;
import com.mrsoft.arabicreference.linguistics.application.ContentRevisionRecorder;
import com.mrsoft.arabicreference.linguistics.domain.editorial.EditorialGuards;
import com.mrsoft.arabicreference.linguistics.domain.editorial.EditorialWorkflow;
import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import com.mrsoft.arabicreference.linguistics.domain.text.ArabicTextNormalizer;
import com.mrsoft.arabicreference.linguistics.domain.text.ContentSlugs;
import com.mrsoft.arabicreference.shared.kernel.exception.ConflictException;
import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import com.mrsoft.arabicreference.shared.kernel.exception.ForbiddenOperationException;
import com.mrsoft.arabicreference.shared.kernel.exception.ResourceNotFoundException;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import com.mrsoft.arabicreference.shared.kernel.id.Ids;
import com.mrsoft.arabicreference.shared.kernel.security.AuthenticatedAccess;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import java.sql.Timestamp;
import org.springframework.jdbc.core.JdbcTemplate;
import com.mrsoft.arabicreference.source.domain.LicensePolicy;
import com.mrsoft.arabicreference.source.domain.LicenseType;
import com.mrsoft.arabicreference.source.domain.SourceType;
import com.mrsoft.arabicreference.source.infrastructure.persistence.ReferenceSourceEntity;
import com.mrsoft.arabicreference.source.infrastructure.persistence.ReferenceSourceRepository;
import com.mrsoft.arabicreference.source.infrastructure.persistence.SourceCitationEntity;
import com.mrsoft.arabicreference.source.infrastructure.persistence.SourceCitationRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SourceAdminService {

    private final ReferenceSourceRepository sources;
    private final SourceCitationRepository citations;
    private final AuthorizationService authorization;
    private final AuditRecorder audit;
    private final ContentRevisionRecorder revisions;
    private final TimeProvider timeProvider;
    private final JdbcTemplate jdbc;
    private final ArabicTextNormalizer normalizer = new ArabicTextNormalizer();

    public SourceAdminService(
            ReferenceSourceRepository sources,
            SourceCitationRepository citations,
            AuthorizationService authorization,
            AuditRecorder audit,
            ContentRevisionRecorder revisions,
            TimeProvider timeProvider,
            JdbcTemplate jdbc) {
        this.sources = sources;
        this.citations = citations;
        this.authorization = authorization;
        this.audit = audit;
        this.revisions = revisions;
        this.timeProvider = timeProvider;
        this.jdbc = jdbc;
    }

    @Transactional(readOnly = true)
    @org.springframework.security.access.prepost.PreAuthorize("@authz.has('" + PermissionCatalog.SOURCE_VIEW + "')")
    public SourceViews.PageResult<SourceViews.SourceView> list(int page, int size) {
        int bounded = bound(page, size);
        var result = sources.findAllByOrderByTitleAsc(PageRequest.of(page, bounded, Sort.by("title")));
        return new SourceViews.PageResult<>(result.stream().map(this::view).toList(), page, bounded, result.getTotalElements());
    }

    @Transactional(readOnly = true)
    @org.springframework.security.access.prepost.PreAuthorize("@authz.has('" + PermissionCatalog.SOURCE_VIEW + "')")
    public SourceViews.SourceView get(UUID id) {
        return view(sources.findById(id).orElseThrow(this::missing));
    }

    @Transactional
    @org.springframework.security.access.prepost.PreAuthorize("@authz.has('" + PermissionCatalog.SOURCE_MANAGE + "')")
    public SourceViews.SourceView create(SourceViews.SourceDraft draft) {
        AuthenticatedAccess actor = authorization.requireAccess();
        ReferenceSourceEntity source = new ReferenceSourceEntity();
        source.setId(Ids.random());
        apply(source, draft);
        source.setStatus(PublicationStatus.DRAFT);
        source.setSlug(ContentSlugs.of(normalizer.normalize(draft.title()).normalizedText(), source.getId()));
        stampNew(source, actor.userId());
        sources.saveAndFlush(source);
        audit.record(actor.userId(), AuditEventType.SOURCE_CREATED, "reference_source", source.getId().toString(), Map.of("license", source.getLicenseType().name()));
        return view(source);
    }

    @Transactional
    @org.springframework.security.access.prepost.PreAuthorize("@authz.has('" + PermissionCatalog.SOURCE_MANAGE + "')")
    public SourceViews.SourceView update(UUID id, long version, SourceViews.SourceDraft draft) {
        AuthenticatedAccess actor = authorization.requireAccess();
        ReferenceSourceEntity source = locked(id, version);
        openForEdit(source, actor.userId(), "source update");
        revisions.record("reference_source", source.getId(), sourceSnapshot(source), actor.userId(), "source update");
        apply(source, draft);
        source.setUpdatedAt(timeProvider.now());
        source.setUpdatedBy(actor.userId());
        sources.saveAndFlush(source);
        audit.record(actor.userId(), AuditEventType.SOURCE_UPDATED, "reference_source", source.getId().toString(), Map.of("status", source.getStatus().name()));
        return view(source);
    }

    @Transactional
    @org.springframework.security.access.prepost.PreAuthorize("@authz.has('" + PermissionCatalog.CITATION_MANAGE + "')")
    public SourceViews.CitationView cite(UUID sourceId, SourceViews.CitationDraft draft) {
        AuthenticatedAccess actor = authorization.requireAccess();
        ReferenceSourceEntity source = sources.findById(sourceId).orElseThrow(this::missing);
        validatePages(draft.pageFrom(), draft.pageTo());
        SourceCitationEntity citation = new SourceCitationEntity();
        citation.setId(Ids.random());
        citation.setSourceId(source.getId());
        citation.setPageFrom(draft.pageFrom());
        citation.setPageTo(draft.pageTo());
        citation.setVolume(blankToNull(draft.volume()));
        citation.setChapter(blankToNull(draft.chapter()));
        citation.setSectionLabel(blankToNull(draft.sectionLabel()));
        citation.setEntryLabel(blankToNull(draft.entryLabel()));
        citation.setSourceLocator(blankToNull(draft.sourceLocator()));
        citation.setQuotedText(blankToNull(draft.quotedText()));
        citation.setPoem(blankToNull(draft.poem()));
        citation.setVerse(blankToNull(draft.verse()));
        citation.setNotes(blankToNull(draft.notes()));
        citation.setCreatedAt(timeProvider.now());
        citation.setCreatedBy(actor.userId());
        citations.saveAndFlush(citation);
        source.setUpdatedAt(timeProvider.now());
        sources.saveAndFlush(source);
        audit.record(actor.userId(), AuditEventType.CITATION_ADDED, "source_citation", citation.getId().toString(), Map.of("sourceId", source.getId().toString()));
        return citationView(citation, source);
    }

    @Transactional
    public SourceViews.SourceView submit(UUID id, long version) {
        return transition(id, version, PermissionCatalog.ENTRY_SUBMIT, EditorialWorkflow::submit, AuditEventType.CONTENT_SUBMITTED, null);
    }

    @Transactional
    public SourceViews.SourceView verify(UUID id, long version) {
        return transition(id, version, PermissionCatalog.ENTRY_REVIEW, EditorialWorkflow::verify, AuditEventType.CONTENT_VERIFIED, "reviewer");
    }

    @Transactional
    public SourceViews.SourceView requestChanges(UUID id, long version, String reason) {
        requireReason(reason);
        return transition(id, version, PermissionCatalog.ENTRY_REVIEW, EditorialWorkflow::requestChanges, AuditEventType.CONTENT_CHANGES_REQUESTED, "reviewer", reason);
    }

    @Transactional
    public SourceViews.SourceView publish(UUID id, long version) {
        ReferenceSourceEntity source = prepare(id, version, PermissionCatalog.ENTRY_PUBLISH);
        EditorialGuards.requireDifferentPerson(source.getCreatedBy(), authorization.requireAccess().userId(), "The creator cannot publish their own source.");
        EditorialGuards.requireDifferentPerson(source.getReviewedBy(), authorization.requireAccess().userId(), "The reviewer cannot publish the same source.");
        if (!LicensePolicy.allowsPublicAttribution(source.getLicenseType())) {
            throw new ForbiddenOperationException("A restricted or unknown license cannot be published.");
        }
        source.setStatus(EditorialWorkflow.publish(source.getStatus()));
        source.setPublishedSnapshot(publicSnapshot(source));
        finish(source, AuditEventType.CONTENT_PUBLISHED, null);
        return view(source);
    }

    @Transactional
    public SourceViews.SourceView archive(UUID id, long version) {
        return transition(id, version, PermissionCatalog.ENTRY_ARCHIVE, EditorialWorkflow::archive, AuditEventType.CONTENT_ARCHIVED, null);
    }

    @Transactional(readOnly = true)
    public List<SourceViews.CitationView> citations(List<UUID> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return citations.findByIdIn(ids).stream().map(citation -> citationView(citation, sources.findById(citation.getSourceId()).orElseThrow(this::missing))).toList();
    }

    private SourceViews.SourceView transition(
            UUID id,
            long version,
            String permission,
            java.util.function.Function<PublicationStatus, PublicationStatus> move,
            AuditEventType event,
            String eyes) {
        return transition(id, version, permission, move, event, eyes, null);
    }

    private SourceViews.SourceView transition(
            UUID id,
            long version,
            String permission,
            java.util.function.Function<PublicationStatus, PublicationStatus> move,
            AuditEventType event,
            String eyes,
            String reason) {
        ReferenceSourceEntity source = prepare(id, version, permission);
        AuthenticatedAccess actor = authorization.requireAccess();
        if ("reviewer".equals(eyes)) {
            EditorialGuards.requireDifferentPerson(source.getCreatedBy(), actor.userId(), "The creator cannot review their own source.");
            source.setReviewedBy(actor.userId());
        }
        source.setStatus(move.apply(source.getStatus()));
        if (reason != null) {
            source.setChangeReason(reason);
        }
        finish(source, event, reason);
        return view(source);
    }

    private ReferenceSourceEntity prepare(UUID id, long version, String permission) {
        if (!authorization.has(permission)) {
            throw new ForbiddenOperationException("You do not have permission to perform this operation.");
        }
        return locked(id, version);
    }

    private void finish(ReferenceSourceEntity source, AuditEventType event, String reason) {
        AuthenticatedAccess actor = authorization.requireAccess();
        source.setUpdatedAt(timeProvider.now());
        source.setUpdatedBy(actor.userId());
        sources.saveAndFlush(source);
        audit.record(actor.userId(), event, "reference_source", source.getId().toString(), Map.of("status", source.getStatus().name()));
        audit.record(actor.userId(), AuditEventType.SOURCE_STATUS_CHANGED, "reference_source", source.getId().toString(), Map.of("status", source.getStatus().name()));
        if (reason != null) {
            source.setChangeReason(reason);
        }
    }

    private void openForEdit(ReferenceSourceEntity source, UUID actor, String reason) {
        if (!EditorialWorkflow.editable(source.getStatus())) {
            throw new ConflictException("This source cannot be edited while it is " + source.getStatus() + ".");
        }
        if (source.getStatus() == PublicationStatus.PUBLISHED) {
            revisions.record("reference_source", source.getId(), sourceSnapshot(source), actor, reason);
            source.setStatus(PublicationStatus.DRAFT);
            source.setReviewedBy(null);
        }
    }

    private void apply(ReferenceSourceEntity source, SourceViews.SourceDraft draft) {
        if (draft.title() == null || draft.title().isBlank() || draft.title().length() > 300) {
            throw new ValidationException("A source title is required.", List.of(new FieldErrorDetail("title", "Enter a title.")));
        }
        if (draft.attributionText() == null || draft.attributionText().isBlank()) {
            throw new ValidationException("Attribution text is required.", List.of(new FieldErrorDetail("attributionText", "Enter attribution text.")));
        }
        if (draft.publicationYear() != null && (draft.publicationYear() < 1 || draft.publicationYear() > 2100)) {
            throw new ValidationException("Publication year is invalid.", List.of(new FieldErrorDetail("publicationYear", "Use a year from 1 to 2100.")));
        }
        boolean domain = draft.licenseType() == LicenseType.PUBLIC_DOMAIN || draft.licenseType() == LicenseType.CC0;
        if (draft.publicDomain() && !domain) {
            throw new ValidationException("Only public-domain licenses can be marked public domain.", List.of(new FieldErrorDetail("publicDomain", "License does not match.")));
        }
        source.setSourceType(draft.sourceType() == null ? SourceType.OTHER : draft.sourceType());
        source.setTitle(draft.title().trim());
        source.setAuthor(blankToNull(draft.author()));
        source.setPublisher(blankToNull(draft.publisher()));
        source.setEdition(blankToNull(draft.edition()));
        source.setPublicationYear(draft.publicationYear());
        source.setIsbn(blankToNull(draft.isbn()));
        source.setUrl(blankToNull(draft.url()));
        source.setLicenseType(draft.licenseType());
        source.setPublicDomain(domain && draft.publicDomain());
        source.setAttributionText(draft.attributionText().trim());
        source.setNotes(blankToNull(draft.notes()));
        source.setIdentityKey(identity(source.getTitle(), source.getAuthor(), source.getEdition()));
    }

    private Map<String, Object> publicSnapshot(ReferenceSourceEntity source) {
        Map<String, Object> snapshot = sourceSnapshot(source);
        snapshot.remove("notes");
        return snapshot;
    }

    private Map<String, Object> sourceSnapshot(ReferenceSourceEntity source) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("title", source.getTitle());
        snapshot.put("author", source.getAuthor());
        snapshot.put("license", source.getLicenseType().name());
        snapshot.put("status", source.getStatus().name());
        snapshot.put("attributionText", source.getAttributionText());
        return snapshot;
    }

    private SourceViews.SourceView view(ReferenceSourceEntity source) {
        return new SourceViews.SourceView(
                source.getId(),
                source.getSourceType().name(),
                source.getTitle(),
                source.getAuthor(),
                source.getPublisher(),
                source.getEdition(),
                source.getPublicationYear(),
                source.getIsbn(),
                source.getUrl(),
                source.getLicenseType().name(),
                source.isPublicDomain(),
                source.getAttributionText(),
                source.getNotes(),
                source.getStatus().name(),
                source.getSlug(),
                source.getVersion(),
                LicensePolicy.allowsPublicAttribution(source.getLicenseType()),
                citationCount(source.getId()),
                lastUsed(source.getId()),
                licenseLabel(source.getLicenseType()));
    }

    private SourceViews.CitationView citationView(SourceCitationEntity citation, ReferenceSourceEntity source) {
        return new SourceViews.CitationView(
                citation.getId(),
                source.getId(),
                source.getTitle(),
                source.getAuthor(),
                source.getEdition(),
                source.getPublicationYear(),
                citation.getPageFrom(),
                citation.getPageTo(),
                citation.getVolume(),
                citation.getChapter(),
                citation.getSectionLabel(),
                citation.getEntryLabel(),
                citation.getSourceLocator(),
                citation.getQuotedText(),
                citation.getPoem(),
                citation.getVerse(),
                source.getAttributionText(),
                source.getLicenseType().name(),
                source.getStatus().name(),
                LicensePolicy.allowsPublicAttribution(source.getLicenseType()));
    }

    private ReferenceSourceEntity locked(UUID id, long version) {
        ReferenceSourceEntity source = sources.lockById(id).orElseThrow(this::missing);
        if (source.getVersion() != version) {
            throw new ConflictException("The record was updated by someone else. Reload and try again.");
        }
        return source;
    }

    private void stampNew(ReferenceSourceEntity source, UUID actor) {
        var now = timeProvider.now();
        source.setCreatedAt(now);
        source.setUpdatedAt(now);
        source.setCreatedBy(actor);
        source.setUpdatedBy(actor);
    }

    private void validatePages(Integer from, Integer to) {
        if ((from != null && from < 1) || (to != null && to < 1) || (from != null && to != null && from > to)) {
            throw new ValidationException("Citation page range is invalid.", List.of(new FieldErrorDetail("pageFrom", "Pages must be positive and the start cannot pass the end.")));
        }
    }

    private void requireReason(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new ValidationException("A reason is required.", List.of(new FieldErrorDetail("reason", "Explain what must change.")));
        }
    }

    private String identity(String title, String author, String edition) {
        return part(title) + "|" + part(author) + "|" + part(edition);
    }

    private String part(String value) {
        return normalizer.normalize(value == null ? "" : value).normalizedText();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static int bound(int page, int size) {
        if (page < 0 || size < 1 || size > 50) {
            throw new ValidationException("Page request is invalid.", List.of(new FieldErrorDetail("page", "Page size must be from 1 to 50.")));
        }
        return size;
    }

    @Transactional(readOnly = true)
    @org.springframework.security.access.prepost.PreAuthorize("@authz.has('" + PermissionCatalog.SOURCE_VIEW + "')")
    public List<SourceViews.DuplicateGroup> duplicates() {
        List<String> keys = jdbc.query("""
                select identity_key from reference_source
                group by identity_key having count(*) > 1
                """, (row, index) -> row.getString(1));
        List<SourceViews.DuplicateGroup> groups = new java.util.ArrayList<>();
        for (String key : keys) {
            List<SourceViews.SourceView> matches = jdbc.query("select id from reference_source where identity_key = ?",
                    (row, index) -> row.getObject(1, UUID.class), key).stream()
                    .map(match -> view(sources.findById(match).orElseThrow(this::missing)))
                    .toList();
            groups.add(new SourceViews.DuplicateGroup(key, matches));
        }
        return groups;
    }

    @Transactional(readOnly = true)
    @org.springframework.security.access.prepost.PreAuthorize("@authz.has('" + PermissionCatalog.SOURCE_USAGE_VIEW + "')")
    public List<SourceViews.SourceUsage> usage(UUID sourceId) {
        if (!sources.existsById(sourceId)) {
            throw missing();
        }
        return jdbc.query("""
                select 'DICTIONARY_ENTRY' as content_type, e.id, e.lemma_original as title, e.status
                from entry_citation link
                join source_citation sc on sc.id = link.citation_id
                join lexical_entry e on e.id = link.entry_id
                where sc.source_id = ?
                union all
                select 'DICTIONARY_ENTRY', e.id, e.lemma_original, e.status
                from sense_citation link
                join source_citation sc on sc.id = link.citation_id
                join lexical_sense sense on sense.id = link.sense_id
                join lexical_entry e on e.id = sense.lexical_entry_id
                where sc.source_id = ?
                union all
                select 'GRAMMAR_RULE', rule.id, rule.title_original, rule.status
                from grammar_rule_citation link
                join source_citation sc on sc.id = link.citation_id
                join grammar_rule rule on rule.id = link.rule_id
                where sc.source_id = ?
                union all
                select 'SPELLING_RULE', rule.id, rule.title_original, rule.status
                from spelling_rule_citation link
                join source_citation sc on sc.id = link.citation_id
                join spelling_rule rule on rule.id = link.owner_id
                where sc.source_id = ?
                union all
                select 'RHETORIC_DEVICE', device.id, device.name_original, device.status
                from rhetoric_device_citation link
                join source_citation sc on sc.id = link.citation_id
                join rhetoric_device device on device.id = link.owner_id
                where sc.source_id = ?
                union all
                select 'LITERARY_WORK', work.id, work.title_original, work.status
                from literary_work_citation link
                join source_citation sc on sc.id = link.citation_id
                join literary_work work on work.id = link.owner_id
                where sc.source_id = ?
                union all
                select 'ARTICLE', article.id, article.title_original, article.status
                from article_citation link
                join source_citation sc on sc.id = link.citation_id
                join article article on article.id = link.article_id
                where sc.source_id = ?
                """, (row, index) -> {
            String type = row.getString("content_type");
            UUID id = row.getObject("id", UUID.class);
            return new SourceViews.SourceUsage(type, id, row.getString("title"), row.getString("status"), href(type, id));
        }, sourceId, sourceId, sourceId, sourceId, sourceId, sourceId, sourceId);
    }

    @Transactional
    @org.springframework.security.access.prepost.PreAuthorize("@authz.has('" + PermissionCatalog.SOURCE_MANAGE + "')")
    public void remove(UUID id, long version) {
        ReferenceSourceEntity source = locked(id, version);
        if (citationCount(id) > 0) {
            throw new ConflictException("المصدر مستخدم ولا يمكن حذفه.");
        }
        if (source.getStatus() != PublicationStatus.DRAFT) {
            throw new ConflictException("أرشف المصدر بدل حذفه.");
        }
        sources.delete(source);
    }

    private long citationCount(UUID sourceId) {
        Long count = jdbc.queryForObject("select count(*) from source_citation where source_id = ?", Long.class, sourceId);
        return count == null ? 0L : count;
    }

    private String lastUsed(UUID sourceId) {
        Timestamp timestamp = jdbc.queryForObject(
                "select max(created_at) from source_citation where source_id = ?",
                Timestamp.class,
                sourceId);
        return timestamp == null ? null : timestamp.toInstant().toString();
    }

    private static String licenseLabel(LicenseType license) {
        return switch (license) {
            case PUBLIC_DOMAIN -> "ملكية عامة";
            case CC0 -> "ملكية عامة CC0";
            case CC_BY -> "نسب المصنف";
            case CC_BY_SA -> "نسب المصنف - المشاركة بالمثل";
            case PERMISSION_GRANTED -> "إذن ممنوح";
            case RESTRICTED -> "مقيد";
            case UNKNOWN -> "غير معروف";
        };
    }

    private static String href(String type, UUID id) {
        return switch (type) {
            case "DICTIONARY_ENTRY" -> "/admin/dictionary/" + id;
            case "GRAMMAR_RULE" -> "/admin/grammar/rules/" + id;
            case "SPELLING_RULE" -> "/admin/spelling";
            case "RHETORIC_DEVICE" -> "/admin/rhetoric";
            case "LITERARY_WORK" -> "/admin/literature";
            case "ARTICLE" -> "/admin/articles";
            default -> "/admin/sources";
        };
    }

    private ResourceNotFoundException missing() {
        return new ResourceNotFoundException("Source was not found.");
    }
}
