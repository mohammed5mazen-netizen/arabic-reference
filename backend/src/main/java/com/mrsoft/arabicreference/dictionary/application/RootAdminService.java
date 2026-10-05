package com.mrsoft.arabicreference.dictionary.application;

import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.AdminRoot;
import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.PageResult;
import com.mrsoft.arabicreference.dictionary.domain.ArabicLexicalText;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LinguisticRootEntity;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LinguisticRootRepository;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.RootCitationEntity;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.RootCitationRepository;
import com.mrsoft.arabicreference.identity.application.AuditRecorder;
import com.mrsoft.arabicreference.identity.application.AuthorizationService;
import com.mrsoft.arabicreference.identity.domain.AuditEventType;
import com.mrsoft.arabicreference.identity.domain.PermissionCatalog;
import com.mrsoft.arabicreference.linguistics.application.ArabicTextNormalizationService;
import com.mrsoft.arabicreference.linguistics.application.ContentRevisionRecorder;
import com.mrsoft.arabicreference.linguistics.domain.editorial.EditorialGuards;
import com.mrsoft.arabicreference.linguistics.domain.editorial.EditorialWorkflow;
import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import com.mrsoft.arabicreference.linguistics.domain.text.ContentSlugs;
import com.mrsoft.arabicreference.shared.kernel.exception.ConflictException;
import com.mrsoft.arabicreference.shared.kernel.exception.FieldErrorDetail;
import com.mrsoft.arabicreference.shared.kernel.exception.ForbiddenOperationException;
import com.mrsoft.arabicreference.shared.kernel.exception.ResourceNotFoundException;
import com.mrsoft.arabicreference.shared.kernel.exception.ValidationException;
import com.mrsoft.arabicreference.shared.kernel.id.Ids;
import com.mrsoft.arabicreference.shared.kernel.security.AuthenticatedAccess;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.source.application.SourceAdminService;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RootAdminService {

    private final LinguisticRootRepository roots;
    private final RootCitationRepository citations;
    private final SourceAdminService sources;
    private final ArabicTextNormalizationService text;
    private final AuthorizationService authorization;
    private final AuditRecorder audit;
    private final ContentRevisionRecorder revisions;
    private final TimeProvider timeProvider;
    private final DictionarySearchIndexer searchIndexer;

    public RootAdminService(
            LinguisticRootRepository roots,
            RootCitationRepository citations,
            SourceAdminService sources,
            ArabicTextNormalizationService text,
            AuthorizationService authorization,
            AuditRecorder audit,
            ContentRevisionRecorder revisions,
            TimeProvider timeProvider,
            DictionarySearchIndexer searchIndexer) {
        this.roots = roots;
        this.citations = citations;
        this.sources = sources;
        this.text = text;
        this.authorization = authorization;
        this.audit = audit;
        this.revisions = revisions;
        this.timeProvider = timeProvider;
        this.searchIndexer = searchIndexer;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.ROOT_VIEW + "')")
    public PageResult<AdminRoot> list(int page, int size) {
        int bounded = bound(page, size);
        var result = roots.findAllByOrderByRootNormalizedAsc(PageRequest.of(page, bounded));
        return new PageResult<>(result.stream().map(this::view).toList(), page, bounded, result.getTotalElements());
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.ROOT_MANAGE + "')")
    public AdminRoot create(String original, String notes) {
        AuthenticatedAccess actor = authorization.requireAccess();
        String letters = letters(original, notes);
        if (roots.findByRootNormalized(letters).isPresent()) {
            throw new ConflictException("This root already exists.");
        }
        LinguisticRootEntity root = new LinguisticRootEntity();
        root.setId(Ids.random());
        root.setRootOriginal(original.trim());
        root.setRootNormalized(letters);
        root.setRadicalCount((short) letters.codePointCount(0, letters.length()));
        root.setNotes(blank(notes));
        root.setStatus(PublicationStatus.DRAFT);
        root.setSlug(ContentSlugs.of(letters, root.getId()));
        Instant now = timeProvider.now();
        root.setCreatedAt(now);
        root.setUpdatedAt(now);
        root.setCreatedBy(actor.userId());
        root.setUpdatedBy(actor.userId());
        roots.saveAndFlush(root);
        audit.record(actor.userId(), AuditEventType.ROOT_CREATED, "linguistic_root", root.getId().toString(), Map.of("root", letters));
        return view(root);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.ROOT_MANAGE + "')")
    public AdminRoot update(UUID id, long version, String original, String notes) {
        AuthenticatedAccess actor = authorization.requireAccess();
        LinguisticRootEntity root = locked(id, version);
        open(root, actor.userId());
        String letters = letters(original, notes);
        roots.findByRootNormalized(letters).filter(existing -> !existing.getId().equals(root.getId())).ifPresent(existing -> {
            throw new ConflictException("This root already exists.");
        });
        revisions.record("linguistic_root", root.getId(), Map.of("root", root.getRootOriginal(), "status", root.getStatus().name()), actor.userId(), "root update");
        root.setRootOriginal(original.trim());
        root.setRootNormalized(letters);
        root.setRadicalCount((short) letters.codePointCount(0, letters.length()));
        root.setNotes(blank(notes));
        touch(root, actor.userId());
        roots.saveAndFlush(root);
        return view(root);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.CITATION_MANAGE + "')")
    public AdminRoot linkCitation(UUID id, long version, UUID citationId) {
        AuthenticatedAccess actor = authorization.requireAccess();
        LinguisticRootEntity root = locked(id, version);
        if (sources.citations(List.of(citationId)).isEmpty()) {
            throw new ResourceNotFoundException("Citation was not found.");
        }
        if (!citations.existsByOwnerIdAndCitationId(id, citationId)) {
            citations.save(new RootCitationEntity(id, citationId));
        }
        touch(root, actor.userId());
        roots.saveAndFlush(root);
        audit.record(actor.userId(), AuditEventType.CITATION_ADDED, "linguistic_root", id.toString(), Map.of("citationId", citationId.toString()));
        return view(root);
    }

    @Transactional
    public AdminRoot submit(UUID id, long version) {
        return move(id, version, PermissionCatalog.ENTRY_SUBMIT, EditorialWorkflow::submit, AuditEventType.CONTENT_SUBMITTED, false);
    }

    @Transactional
    public AdminRoot verify(UUID id, long version) {
        return move(id, version, PermissionCatalog.ENTRY_REVIEW, EditorialWorkflow::verify, AuditEventType.CONTENT_VERIFIED, true);
    }

    @Transactional
    public AdminRoot publish(UUID id, long version) {
        searchIndexer.lock();
        if (!authorization.has(PermissionCatalog.ENTRY_PUBLISH)) {
            throw new ForbiddenOperationException("You do not have permission to perform this operation.");
        }
        AuthenticatedAccess actor = authorization.requireAccess();
        LinguisticRootEntity root = locked(id, version);
        EditorialGuards.requireDifferentPerson(root.getCreatedBy(), actor.userId(), "The creator cannot publish their own root.");
        EditorialGuards.requireDifferentPerson(root.getReviewedBy(), actor.userId(), "The reviewer cannot publish the same root.");
        root.setStatus(EditorialWorkflow.publish(root.getStatus()));
        root.setPublishedNormalized(root.getRootNormalized());
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("original", root.getRootOriginal());
        snapshot.put("normalized", root.getRootNormalized());
        snapshot.put("radicalCount", root.getRadicalCount());
        snapshot.put("slug", root.getSlug());
        root.setPublishedSnapshot(snapshot);
        touch(root, actor.userId());
        roots.saveAndFlush(root);
        searchIndexer.onRootPublished(root);
        audit.record(actor.userId(), AuditEventType.CONTENT_PUBLISHED, "linguistic_root", root.getId().toString(), Map.of("status", root.getStatus().name()));
        return view(root);
    }

    @Transactional
    public AdminRoot archive(UUID id, long version) {
        searchIndexer.lock();
        return move(id, version, PermissionCatalog.ENTRY_ARCHIVE, EditorialWorkflow::archive, AuditEventType.CONTENT_ARCHIVED, false);
    }

    private AdminRoot move(UUID id, long version, String permission, Function<PublicationStatus, PublicationStatus> transition, AuditEventType event, boolean reviewer) {
        if (!authorization.has(permission)) {
            throw new ForbiddenOperationException("You do not have permission to perform this operation.");
        }
        AuthenticatedAccess actor = authorization.requireAccess();
        LinguisticRootEntity root = locked(id, version);
        if (reviewer) {
            EditorialGuards.requireDifferentPerson(root.getCreatedBy(), actor.userId(), "The creator cannot review their own root.");
            root.setReviewedBy(actor.userId());
        }
        root.setStatus(transition.apply(root.getStatus()));
        touch(root, actor.userId());
        roots.saveAndFlush(root);
        if (root.getStatus() == PublicationStatus.ARCHIVED) {
            searchIndexer.onRootArchived(root.getId());
        }
        audit.record(actor.userId(), event, "linguistic_root", root.getId().toString(), Map.of("status", root.getStatus().name()));
        return view(root);
    }

    private void open(LinguisticRootEntity root, UUID actor) {
        if (!EditorialWorkflow.editable(root.getStatus())) {
            throw new ConflictException("This root cannot be edited while it is " + root.getStatus() + ".");
        }
        if (root.getStatus() == PublicationStatus.PUBLISHED) {
            revisions.record("linguistic_root", root.getId(), Map.of("root", root.getRootOriginal(), "status", root.getStatus().name()), actor, "reopen");
            root.setStatus(PublicationStatus.DRAFT);
            root.setReviewedBy(null);
        }
    }

    private String letters(String original, String notes) {
        if (original == null || original.isBlank() || original.length() > 32) {
            throw new ValidationException("Enter an Arabic root.", List.of(new FieldErrorDetail("root", "Enter an Arabic root.")));
        }
        String normalized = text.normalize(original).normalizedText().replace(" ", "");
        return ArabicLexicalText.requireRoot(normalized, notes);
    }

    private LinguisticRootEntity locked(UUID id, long version) {
        LinguisticRootEntity root = roots.lockById(id).orElseThrow(() -> new ResourceNotFoundException("Root was not found."));
        if (root.getVersion() != version) {
            throw new ConflictException("The record was updated by someone else. Reload and try again.");
        }
        return root;
    }

    private void touch(LinguisticRootEntity root, UUID actor) {
        root.setUpdatedAt(timeProvider.now());
        root.setUpdatedBy(actor);
    }

    private AdminRoot view(LinguisticRootEntity root) {
        return new AdminRoot(root.getId(), root.getRootOriginal(), root.getRootNormalized(), root.getRadicalCount(), root.getNotes(), root.getStatus().name(), root.getSlug(), root.getVersion(), root.getPublishedSnapshot() != null && root.getStatus() != PublicationStatus.ARCHIVED);
    }

    private static String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static int bound(int page, int size) {
        if (page < 0 || size < 1 || size > 50) {
            throw new ValidationException("Page request is invalid.", List.of(new FieldErrorDetail("page", "Page size must be from 1 to 50.")));
        }
        return size;
    }
}
