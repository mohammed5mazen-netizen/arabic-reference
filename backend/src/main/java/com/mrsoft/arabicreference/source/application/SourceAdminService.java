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
    private final ArabicTextNormalizer normalizer = new ArabicTextNormalizer();

    public SourceAdminService(
            ReferenceSourceRepository sources,
            SourceCitationRepository citations,
            AuthorizationService authorization,
            AuditRecorder audit,
            ContentRevisionRecorder revisions,
            TimeProvider timeProvider) {
        this.sources = sources;
        this.citations = citations;
        this.authorization = authorization;
        this.audit = audit;
        this.revisions = revisions;
        this.timeProvider = timeProvider;
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
                LicensePolicy.allowsPublicAttribution(source.getLicenseType()));
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

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static int bound(int page, int size) {
        if (page < 0 || size < 1 || size > 50) {
            throw new ValidationException("Page request is invalid.", List.of(new FieldErrorDetail("page", "Page size must be from 1 to 50.")));
        }
        return size;
    }

    private ResourceNotFoundException missing() {
        return new ResourceNotFoundException("Source was not found.");
    }
}
