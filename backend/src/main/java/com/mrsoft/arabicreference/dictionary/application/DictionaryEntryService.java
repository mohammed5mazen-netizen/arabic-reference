package com.mrsoft.arabicreference.dictionary.application;

import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.AdminEntry;
import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.AdminExample;
import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.AdminForm;
import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.AdminRelation;
import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.AdminSense;
import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.Attribution;
import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.EntryDraft;
import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.ExampleDraft;
import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.FormDraft;
import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.PageResult;
import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.PublicEntry;
import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.PublicExample;
import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.PublicForm;
import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.PublicRelation;
import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.PublicRoot;
import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.PublicRootRef;
import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.PublicSense;
import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.RelationDraft;
import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.RevisionView;
import com.mrsoft.arabicreference.dictionary.application.DictionaryViews.SenseDraft;
import com.mrsoft.arabicreference.dictionary.domain.ArabicLexicalText;
import com.mrsoft.arabicreference.dictionary.domain.ExampleKind;
import com.mrsoft.arabicreference.dictionary.domain.RelationType;
import com.mrsoft.arabicreference.dictionary.domain.VerificationLevel;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.EntryCitationEntity;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.EntryCitationRepository;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LexicalEntryEntity;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LexicalEntryRepository;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LexicalFormEntity;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LexicalFormRepository;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LexicalSenseEntity;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LexicalSenseRepository;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LinguisticRelationEntity;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LinguisticRelationRepository;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LinguisticRootEntity;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.LinguisticRootRepository;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.RelationCitationEntity;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.RelationCitationRepository;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.SenseCitationEntity;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.SenseCitationRepository;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.UsageExampleEntity;
import com.mrsoft.arabicreference.dictionary.infrastructure.persistence.UsageExampleRepository;
import com.mrsoft.arabicreference.identity.application.AuditRecorder;
import com.mrsoft.arabicreference.identity.application.AuthorizationService;
import com.mrsoft.arabicreference.identity.domain.AuditEventType;
import com.mrsoft.arabicreference.identity.domain.PermissionCatalog;
import com.mrsoft.arabicreference.linguistics.application.ArabicTextNormalizationService;
import com.mrsoft.arabicreference.linguistics.application.ContentRevisionRecorder;
import com.mrsoft.arabicreference.linguistics.domain.editorial.EditorialGuards;
import com.mrsoft.arabicreference.linguistics.domain.editorial.EditorialWorkflow;
import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import com.mrsoft.arabicreference.linguistics.domain.text.ArabicText;
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
import com.mrsoft.arabicreference.source.application.SourceViews.CitationView;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DictionaryEntryService {

    private final LexicalEntryRepository entries;
    private final LexicalSenseRepository senses;
    private final LexicalFormRepository forms;
    private final UsageExampleRepository examples;
    private final LinguisticRelationRepository relations;
    private final SenseCitationRepository senseCitations;
    private final EntryCitationRepository entryCitations;
    private final RelationCitationRepository relationCitations;
    private final LinguisticRootRepository roots;
    private final SourceAdminService sources;
    private final ArabicTextNormalizationService text;
    private final AuthorizationService authorization;
    private final AuditRecorder audit;
    private final ContentRevisionRecorder revisions;
    private final TimeProvider timeProvider;
    private final DictionarySearchIndexer searchIndexer;

    public DictionaryEntryService(
            LexicalEntryRepository entries,
            LexicalSenseRepository senses,
            LexicalFormRepository forms,
            UsageExampleRepository examples,
            LinguisticRelationRepository relations,
            SenseCitationRepository senseCitations,
            EntryCitationRepository entryCitations,
            RelationCitationRepository relationCitations,
            LinguisticRootRepository roots,
            SourceAdminService sources,
            ArabicTextNormalizationService text,
            AuthorizationService authorization,
            AuditRecorder audit,
            ContentRevisionRecorder revisions,
            TimeProvider timeProvider,
            DictionarySearchIndexer searchIndexer) {
        this.entries = entries;
        this.senses = senses;
        this.forms = forms;
        this.examples = examples;
        this.relations = relations;
        this.senseCitations = senseCitations;
        this.entryCitations = entryCitations;
        this.relationCitations = relationCitations;
        this.roots = roots;
        this.sources = sources;
        this.text = text;
        this.authorization = authorization;
        this.audit = audit;
        this.revisions = revisions;
        this.timeProvider = timeProvider;
        this.searchIndexer = searchIndexer;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.ENTRY_VIEW + "')")
    public PageResult<AdminEntry> list(String word, int page, int size) {
        int bounded = bound(page, size);
        String lemma = word == null || word.isBlank() ? null : normalize(word, "word").normalizedText();
        var result = entries.search(lemma, PageRequest.of(page, bounded, Sort.by("lemmaNormalized")));
        return new PageResult<>(result.stream().map(entry -> assemble(entry, false)).toList(), page, bounded, result.getTotalElements());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.ENTRY_REVIEW + "')")
    public PageResult<AdminEntry> reviewQueue(int page, int size) {
        int bounded = bound(page, size);
        var result = entries.findByStatus(PublicationStatus.IN_REVIEW, PageRequest.of(page, bounded, Sort.by(Sort.Direction.ASC, "updatedAt")));
        return new PageResult<>(result.stream().map(entry -> assemble(entry, false)).toList(), page, bounded, result.getTotalElements());
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.ENTRY_VIEW + "')")
    public AdminEntry get(UUID id) {
        return assemble(entries.findById(id).orElseThrow(this::missingEntry), true);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.ENTRY_CREATE + "')")
    public AdminEntry create(EntryDraft draft) {
        AuthenticatedAccess actor = authorization.requireAccess();
        ArabicText lemma = normalize(draft.lemma(), "lemma");
        LexicalEntryEntity entry = new LexicalEntryEntity();
        entry.setId(Ids.random());
        entry.setLemmaOriginal(draft.lemma().trim());
        entry.setLemmaNormalized(ArabicLexicalText.requireLemma(lemma.normalizedText()));
        applyIdentity(entry, draft, lemma);
        entry.setStatus(PublicationStatus.DRAFT);
        entry.setSlug(ContentSlugs.of(entry.getLemmaNormalized(), entry.getId()));
        stamp(entry, actor.userId(), true);
        entries.saveAndFlush(entry);
        audit.record(actor.userId(), AuditEventType.DICTIONARY_ENTRY_CREATED, "lexical_entry", entry.getId().toString(), Map.of("lemma", entry.getLemmaNormalized()));
        return assemble(entry, true);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.ENTRY_EDIT + "')")
    public AdminEntry update(UUID id, long version, EntryDraft draft) {
        AuthenticatedAccess actor = authorization.requireAccess();
        LexicalEntryEntity entry = locked(id, version);
        openForEdit(entry, actor.userId(), "entry update");
        ArabicText lemma = normalize(draft.lemma(), "lemma");
        entry.setLemmaOriginal(draft.lemma().trim());
        entry.setLemmaNormalized(ArabicLexicalText.requireLemma(lemma.normalizedText()));
        applyIdentity(entry, draft, lemma);
        touch(entry, actor.userId());
        entries.saveAndFlush(entry);
        audit.record(actor.userId(), AuditEventType.DICTIONARY_ENTRY_UPDATED, "lexical_entry", entry.getId().toString(), Map.of("lemma", entry.getLemmaNormalized()));
        return assemble(entry, true);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.SENSE_MANAGE + "')")
    public AdminEntry addSense(UUID entryId, long version, SenseDraft draft) {
        AuthenticatedAccess actor = authorization.requireAccess();
        LexicalEntryEntity entry = locked(entryId, version);
        openForEdit(entry, actor.userId(), "sense added");
        LexicalSenseEntity sense = new LexicalSenseEntity();
        sense.setId(Ids.random());
        sense.setLexicalEntryId(entry.getId());
        fillSense(sense, draft, senses.maxOrder(entry.getId()) + 1);
        sense.setStatus(PublicationStatus.DRAFT);
        stampSense(sense, actor.userId(), true);
        senses.saveAndFlush(sense);
        touch(entry, actor.userId());
        entries.saveAndFlush(entry);
        audit.record(actor.userId(), AuditEventType.SENSE_ADDED, "lexical_sense", sense.getId().toString(), Map.of("entryId", entry.getId().toString()));
        return assemble(entry, true);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.ENTRY_EDIT + "')")
    public AdminEntry addForm(UUID entryId, long version, FormDraft draft) {
        AuthenticatedAccess actor = authorization.requireAccess();
        LexicalEntryEntity entry = locked(entryId, version);
        openForEdit(entry, actor.userId(), "form added");
        ArabicText form = normalize(draft.originalForm(), "originalForm");
        ArabicLexicalText.requireLemma(form.normalizedText());
        LexicalFormEntity entity = new LexicalFormEntity();
        entity.setId(Ids.random());
        entity.setLexicalEntryId(entry.getId());
        entity.setFormType(draft.formType());
        entity.setOriginalForm(draft.originalForm().trim());
        entity.setNormalizedForm(form.normalizedText());
        entity.setNotes(blank(draft.notes()));
        entity.setStatus(PublicationStatus.DRAFT);
        entity.setDisplayOrder(forms.maxOrder(entry.getId()) + 1);
        Instant now = timeProvider.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setCreatedBy(actor.userId());
        entity.setUpdatedBy(actor.userId());
        forms.saveAndFlush(entity);
        touch(entry, actor.userId());
        entries.saveAndFlush(entry);
        audit.record(actor.userId(), AuditEventType.FORM_ADDED, "lexical_form", entity.getId().toString(), Map.of("entryId", entry.getId().toString(), "formType", draft.formType().name()));
        return assemble(entry, true);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.EXAMPLE_MANAGE + "')")
    public AdminEntry addExample(UUID senseId, long version, ExampleDraft draft) {
        AuthenticatedAccess actor = authorization.requireAccess();
        LexicalSenseEntity sense = senses.findById(senseId).orElseThrow(() -> new ResourceNotFoundException("Sense was not found."));
        LexicalEntryEntity entry = locked(sense.getLexicalEntryId(), version);
        openForEdit(entry, actor.userId(), "example added");
        if (draft.kind() == ExampleKind.QUOTED && draft.citationId() == null) {
            throw invalid("citationId", "A quoted example needs a citation.");
        }
        if (draft.citationId() != null && sources.citations(List.of(draft.citationId())).isEmpty()) {
            throw new ResourceNotFoundException("Citation was not found.");
        }
        ArabicText body = normalize(draft.text(), "text");
        UsageExampleEntity example = new UsageExampleEntity();
        example.setId(Ids.random());
        example.setSenseId(sense.getId());
        example.setExampleKind(draft.kind());
        example.setTextOriginal(draft.text().trim());
        example.setTextNormalized(body.normalizedText());
        example.setExplanation(blank(draft.explanation()));
        example.setCitationId(draft.citationId());
        example.setStatus(PublicationStatus.DRAFT);
        example.setDisplayOrder(draft.displayOrder() == null ? examples.maxOrder(sense.getId()) + 1 : draft.displayOrder());
        Instant now = timeProvider.now();
        example.setCreatedAt(now);
        example.setUpdatedAt(now);
        example.setCreatedBy(actor.userId());
        example.setUpdatedBy(actor.userId());
        examples.saveAndFlush(example);
        touch(entry, actor.userId());
        entries.saveAndFlush(entry);
        return assemble(entry, true);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.RELATION_MANAGE + "')")
    public AdminEntry addRelation(UUID entryId, long version, RelationDraft draft) {
        AuthenticatedAccess actor = authorization.requireAccess();
        LexicalEntryEntity entry = locked(entryId, version);
        openForEdit(entry, actor.userId(), "relation added");
        LexicalEntryEntity target = entries.findById(draft.targetEntryId()).orElseThrow(this::missingEntry);
        if (entry.getId().equals(target.getId())) {
            throw invalid("targetEntryId", "A relation needs two different entries.");
        }
        LinguisticRelationEntity relation = new LinguisticRelationEntity();
        relation.setId(Ids.random());
        relation.setRelationType(draft.relationType());
        relation.setSourceEntryId(entry.getId());
        relation.setTargetEntryId(target.getId());
        relation.setVerificationLevel(draft.verificationLevel() == null ? VerificationLevel.REPORTED : draft.verificationLevel());
        relation.setStatus(PublicationStatus.DRAFT);
        if (draft.relationType() == RelationType.DERIVED_FROM) {
            if (relations.countDerivation(RelationType.DERIVED_FROM, entry.getId(), target.getId()) > 0) {
                throw new ConflictException("This derivation already exists.");
            }
        } else {
            LexicalSenseEntity left = senses.findById(draft.sourceSenseId()).orElseThrow(() -> new ResourceNotFoundException("Sense was not found."));
            LexicalSenseEntity right = senses.findById(draft.targetSenseId()).orElseThrow(() -> new ResourceNotFoundException("Sense was not found."));
            if (!left.getLexicalEntryId().equals(entry.getId()) || !right.getLexicalEntryId().equals(target.getId())) {
                throw invalid("sourceSenseId", "Each sense must belong to its entry.");
            }
            if (relations.countSymmetric(draft.relationType(), left.getId(), right.getId()) > 0) {
                throw new ConflictException("This relation already exists.");
            }
            relation.setSourceSenseId(left.getId());
            relation.setTargetSenseId(right.getId());
        }
        Instant now = timeProvider.now();
        relation.setCreatedAt(now);
        relation.setUpdatedAt(now);
        relation.setCreatedBy(actor.userId());
        relation.setUpdatedBy(actor.userId());
        relations.saveAndFlush(relation);
        touch(entry, actor.userId());
        entries.saveAndFlush(entry);
        audit.record(actor.userId(), AuditEventType.RELATION_ADDED, "linguistic_relation", relation.getId().toString(), Map.of("type", relation.getRelationType().name()));
        return assemble(entry, true);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.CITATION_MANAGE + "')")
    public AdminEntry linkSenseCitation(UUID senseId, long version, UUID citationId) {
        return link(senseId, version, citationId, true);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.CITATION_MANAGE + "')")
    public AdminEntry linkEntryCitation(UUID entryId, long version, UUID citationId) {
        AuthenticatedAccess actor = authorization.requireAccess();
        LexicalEntryEntity entry = locked(entryId, version);
        openForEdit(entry, actor.userId(), "citation linked");
        requireCitation(citationId);
        if (!entryCitations.existsByOwnerIdAndCitationId(entry.getId(), citationId)) {
            entryCitations.save(new EntryCitationEntity(entry.getId(), citationId));
        }
        touch(entry, actor.userId());
        entries.saveAndFlush(entry);
        return assemble(entry, true);
    }

    @Transactional
    @PreAuthorize("@authz.has('" + PermissionCatalog.CITATION_MANAGE + "')")
    public AdminEntry linkRelationCitation(UUID relationId, UUID citationId) {
        LinguisticRelationEntity relation = relations.findById(relationId).orElseThrow(() -> new ResourceNotFoundException("Relation was not found."));
        requireCitation(citationId);
        if (!relationCitations.existsByOwnerIdAndCitationId(relationId, citationId)) {
            relationCitations.save(new RelationCitationEntity(relationId, citationId));
        }
        audit.record(authorization.requireAccess().userId(), AuditEventType.CITATION_ADDED, "linguistic_relation", relationId.toString(), Map.of("citationId", citationId.toString()));
        return assemble(entries.findById(relation.getSourceEntryId()).orElseThrow(this::missingEntry), true);
    }

    @Transactional
    public AdminEntry submit(UUID id, long version) {
        return move(id, version, PermissionCatalog.ENTRY_SUBMIT, EditorialWorkflow::submit, AuditEventType.CONTENT_SUBMITTED, false, null);
    }

    @Transactional
    public AdminEntry requestChanges(UUID id, long version, String reason) {
        if (reason == null || reason.isBlank()) {
            throw invalid("reason", "Explain what must change.");
        }
        return move(id, version, PermissionCatalog.ENTRY_REVIEW, EditorialWorkflow::requestChanges, AuditEventType.CONTENT_CHANGES_REQUESTED, true, reason.trim());
    }

    @Transactional
    public AdminEntry verify(UUID id, long version) {
        return move(id, version, PermissionCatalog.ENTRY_REVIEW, EditorialWorkflow::verify, AuditEventType.CONTENT_VERIFIED, true, null);
    }

    @Transactional
    public AdminEntry publish(UUID id, long version) {
        searchIndexer.lock();
        AuthenticatedAccess actor = authorization.requireAccess();
        if (!authorization.has(PermissionCatalog.ENTRY_PUBLISH)) {
            throw new ForbiddenOperationException("You do not have permission to perform this operation.");
        }
        LexicalEntryEntity entry = locked(id, version);
        EditorialGuards.requireDifferentPerson(entry.getCreatedBy(), actor.userId(), "The creator cannot publish their own entry.");
        EditorialGuards.requireDifferentPerson(entry.getReviewedBy(), actor.userId(), "The reviewer cannot publish the same entry.");
        assertPublishable(entry);
        cascade(entry.getId(), PublicationStatus.VERIFIED, PublicationStatus.PUBLISHED);
        entry.setStatus(EditorialWorkflow.publish(entry.getStatus()));
        entry.setPublishedLemmaNormalized(entry.getLemmaNormalized());
        entry.setPublishedSnapshot(publicSnapshot(entry));
        touch(entry, actor.userId());
        entries.saveAndFlush(entry);
        searchIndexer.onEntryPublished(entry);
        audit.record(actor.userId(), AuditEventType.CONTENT_PUBLISHED, "lexical_entry", entry.getId().toString(), Map.of("status", entry.getStatus().name()));
        return assemble(entry, true);
    }

    @Transactional
    public AdminEntry archive(UUID id, long version) {
        searchIndexer.lock();
        return move(id, version, PermissionCatalog.ENTRY_ARCHIVE, EditorialWorkflow::archive, AuditEventType.CONTENT_ARCHIVED, false, null);
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@authz.has('" + PermissionCatalog.ENTRY_VIEW + "')")
    public List<RevisionView> revisions(UUID id) {
        if (!entries.existsById(id)) {
            throw missingEntry();
        }
        return revisions.history("lexical_entry", id).stream()
                .map(revision -> new RevisionView(revision.getRevisionNumber(), revision.getActorId(), revision.getChangeReason(), revision.getCreatedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public PublicEntry publishedById(UUID id) {
        LexicalEntryEntity entry = entries.findById(id).orElseThrow(this::missingEntry);
        return published(entry);
    }

    @Transactional(readOnly = true)
    public PublicEntry publishedBySlug(String slug) {
        LexicalEntryEntity entry = entries.findBySlug(slug).orElseThrow(this::missingEntry);
        return published(entry);
    }

    @Transactional(readOnly = true)
    public PageResult<DictionaryViews.LookupHit> lookup(String word, int page, int size) {
        if (word == null || word.isBlank() || word.codePointCount(0, word.length()) > 80) {
            throw invalid("word", "Enter an Arabic word of 1 to 80 characters.");
        }
        int bounded = bound(page, size);
        String lemma = ArabicLexicalText.requireLemma(normalize(word, "word").normalizedText());
        var result = entries.findPublishedByLemma(lemma, PublicationStatus.ARCHIVED, PageRequest.of(page, bounded));
        return new PageResult<>(result.stream().map(this::hit).toList(), page, bounded, result.getTotalElements());
    }

    @Transactional(readOnly = true)
    public PublicRoot publishedRoot(String key) {
        String normalized = normalize(key, "root").normalizedText().replace(" ", "");
        LinguisticRootEntity root = roots.findPublished(key, PublicationStatus.ARCHIVED)
                .or(() -> roots.findPublished(normalized, PublicationStatus.ARCHIVED))
                .orElseThrow(() -> new ResourceNotFoundException("Root was not found."));
        List<DictionaryViews.LookupHit> linked = entries.findPublishedByRoot(root.getId(), PublicationStatus.ARCHIVED).stream().map(this::hit).toList();
        return new PublicRoot(root.getRootOriginal(), root.getPublishedNormalized(), root.getRadicalCount(), root.getSlug(), linked);
    }

    private AdminEntry link(UUID senseId, long version, UUID citationId, boolean senseLink) {
        AuthenticatedAccess actor = authorization.requireAccess();
        LexicalSenseEntity sense = senses.findById(senseId).orElseThrow(() -> new ResourceNotFoundException("Sense was not found."));
        LexicalEntryEntity entry = locked(sense.getLexicalEntryId(), version);
        openForEdit(entry, actor.userId(), "citation linked");
        requireCitation(citationId);
        if (senseLink && !senseCitations.existsByOwnerIdAndCitationId(sense.getId(), citationId)) {
            senseCitations.save(new SenseCitationEntity(sense.getId(), citationId));
            audit.record(actor.userId(), AuditEventType.CITATION_ADDED, "lexical_sense", sense.getId().toString(), Map.of("citationId", citationId.toString()));
        }
        touch(entry, actor.userId());
        entries.saveAndFlush(entry);
        return assemble(entry, true);
    }

    private AdminEntry move(
            UUID id,
            long version,
            String permission,
            Function<PublicationStatus, PublicationStatus> transition,
            AuditEventType event,
            boolean reviewer,
            String reason) {
        if (!authorization.has(permission)) {
            throw new ForbiddenOperationException("You do not have permission to perform this operation.");
        }
        AuthenticatedAccess actor = authorization.requireAccess();
        LexicalEntryEntity entry = locked(id, version);
        if (reviewer) {
            EditorialGuards.requireDifferentPerson(entry.getCreatedBy(), actor.userId(), "The creator cannot review their own entry.");
            entry.setReviewedBy(actor.userId());
        }
        PublicationStatus next = transition.apply(entry.getStatus());
        PublicationStatus previous = entry.getStatus();
        cascade(entry.getId(), previous, next);
        entry.setStatus(next);
        entry.setChangeReason(reason);
        touch(entry, actor.userId());
        entries.saveAndFlush(entry);
        if (next == PublicationStatus.ARCHIVED) {
            searchIndexer.onEntryArchived(entry);
        }
        audit.record(actor.userId(), event, "lexical_entry", entry.getId().toString(), Map.of("status", next.name()));
        return assemble(entry, true);
    }

    private void cascade(UUID entryId, PublicationStatus from, PublicationStatus to) {
        for (LexicalSenseEntity sense : senses.findByLexicalEntryIdOrderByDisplayOrderAsc(entryId)) {
            if (matches(sense.getStatus(), from)) {
                sense.setStatus(to);
                sense.setUpdatedAt(timeProvider.now());
            }
        }
        for (LexicalFormEntity form : forms.findByLexicalEntryIdOrderByDisplayOrderAsc(entryId)) {
            if (matches(form.getStatus(), from)) {
                form.setStatus(to);
            }
        }
        List<UUID> senseIds = senses.findByLexicalEntryIdOrderByDisplayOrderAsc(entryId).stream().map(LexicalSenseEntity::getId).toList();
        if (!senseIds.isEmpty()) {
            for (UsageExampleEntity example : examples.findBySenseIdInOrderByDisplayOrderAsc(senseIds)) {
                if (matches(example.getStatus(), from)) {
                    example.setStatus(to);
                }
            }
        }
        for (LinguisticRelationEntity relation : relations.findBySourceEntryId(entryId)) {
            if (matches(relation.getStatus(), from)) {
                relation.setStatus(to);
            }
        }
    }

    private boolean matches(PublicationStatus current, PublicationStatus from) {
        if (from == PublicationStatus.DRAFT) {
            return current == PublicationStatus.DRAFT || current == PublicationStatus.CHANGES_REQUESTED;
        }
        return current == from;
    }

    private void assertPublishable(LexicalEntryEntity entry) {
        List<LexicalSenseEntity> entrySenses = senses.findByLexicalEntryIdOrderByDisplayOrderAsc(entry.getId());
        if (entrySenses.isEmpty()) {
            throw invalid("senses", "A published entry needs at least one sense.");
        }
        if (entry.getRootId() != null) {
            LinguisticRootEntity root = roots.findById(entry.getRootId()).orElseThrow(() -> new ResourceNotFoundException("Root was not found."));
            if (root.getPublishedSnapshot() == null || root.getStatus() == PublicationStatus.ARCHIVED) {
                throw new ConflictException("The root must be published before the entry.");
            }
        }
        Map<UUID, List<UUID>> citationsBySense = citationIdsBySense(entrySenses.stream().map(LexicalSenseEntity::getId).toList());
        List<UUID> citationIds = new ArrayList<>();
        for (LexicalSenseEntity sense : entrySenses) {
            List<UUID> links = citationsBySense.getOrDefault(sense.getId(), List.of());
            if (links.isEmpty()) {
                throw new ConflictException("Every published sense needs at least one citation.");
            }
            citationIds.addAll(links);
        }
        for (CitationView citation : sources.citations(citationIds)) {
            if (!"PUBLISHED".equals(citation.sourceStatus()) || !citation.publishableLicense()) {
                throw new ForbiddenOperationException("A published sense cannot cite a restricted, unknown, or unpublished source.");
            }
        }
    }

    private Map<String, Object> publicSnapshot(LexicalEntryEntity entry) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("id", entry.getId().toString());
        snapshot.put("slug", entry.getSlug());
        snapshot.put("lemmaOriginal", entry.getLemmaOriginal());
        snapshot.put("vocalizedForm", entry.getVocalizedForm());
        snapshot.put("partOfSpeech", entry.getPartOfSpeech().name());
        snapshot.put("gender", entry.getGender() == null ? null : entry.getGender().name());
        snapshot.put("root", rootRef(entry.getRootId()));
        List<LexicalSenseEntity> entrySenses = senses.findByLexicalEntryIdOrderByDisplayOrderAsc(entry.getId());
        List<UUID> senseIds = entrySenses.stream().map(LexicalSenseEntity::getId).toList();
        List<UsageExampleEntity> entryExamples = senseIds.isEmpty() ? List.of() : examples.findBySenseIdInOrderByDisplayOrderAsc(senseIds);
        Map<UUID, List<UUID>> citationsBySense = citationIdsBySense(senseIds);
        List<Map<String, Object>> senseMaps = new ArrayList<>();
        for (LexicalSenseEntity sense : entrySenses) {
            if (sense.getStatus() == PublicationStatus.ARCHIVED) {
                continue;
            }
            Map<String, Object> senseMap = new LinkedHashMap<>();
            senseMap.put("definition", sense.getDefinition());
            senseMap.put("shortDefinition", sense.getShortDefinition());
            senseMap.put("usageLabel", sense.getUsageLabel() == null ? null : sense.getUsageLabel().name());
            senseMap.put("domainLabel", sense.getDomainLabel() == null ? null : sense.getDomainLabel().name());
            senseMap.put("displayOrder", sense.getDisplayOrder());
            senseMap.put("examples", entryExamples.stream().filter(example -> example.getSenseId().equals(sense.getId())).map(example -> {
                Map<String, Object> exampleMap = new LinkedHashMap<>();
                exampleMap.put("textOriginal", example.getTextOriginal());
                exampleMap.put("explanation", example.getExplanation());
                exampleMap.put("kind", example.getExampleKind().name());
                return exampleMap;
            }).toList());
            List<UUID> ids = citationsBySense.getOrDefault(sense.getId(), List.of());
            senseMap.put("sources", sources.citations(ids).stream().map(this::attributionMap).toList());
            senseMaps.add(senseMap);
        }
        snapshot.put("senses", senseMaps);
        snapshot.put("forms", forms.findByLexicalEntryIdOrderByDisplayOrderAsc(entry.getId()).stream().map(form -> {
            Map<String, Object> formMap = new LinkedHashMap<>();
            formMap.put("formType", form.getFormType().name());
            formMap.put("originalForm", form.getOriginalForm());
            return formMap;
        }).toList());
        snapshot.put("relations", relationMaps(entry));
        return snapshot;
    }

    private List<Map<String, Object>> relationMaps(LexicalEntryEntity entry) {
        List<Map<String, Object>> maps = new ArrayList<>();
        for (LinguisticRelationEntity relation : relations.findTouching(entry.getId())) {
            if (relation.getStatus() != PublicationStatus.PUBLISHED && !relation.getSourceEntryId().equals(entry.getId())) {
                continue;
            }
            if (relation.getSourceEntryId().equals(entry.getId()) && relation.getStatus() != PublicationStatus.PUBLISHED) {
                continue;
            }
            UUID otherId = relation.getSourceEntryId().equals(entry.getId()) ? relation.getTargetEntryId() : relation.getSourceEntryId();
            LexicalEntryEntity other = entries.findById(otherId).orElse(null);
            if (other == null || other.getPublishedLemmaNormalized() == null || other.getStatus() == PublicationStatus.ARCHIVED) {
                continue;
            }
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("relationType", relation.getRelationType().name());
            Object publishedLemma = other.getPublishedSnapshot() == null ? null : other.getPublishedSnapshot().get("lemmaOriginal");
            map.put("otherLemma", publishedLemma == null ? other.getLemmaOriginal() : String.valueOf(publishedLemma));
            map.put("otherSlug", other.getSlug());
            map.put("direction", relation.getSourceEntryId().equals(entry.getId()) ? "outgoing" : "incoming");
            maps.add(map);
        }
        return maps;
    }

    private Map<String, Object> rootRef(UUID rootId) {
        if (rootId == null) {
            return null;
        }
        LinguisticRootEntity root = roots.findById(rootId).orElse(null);
        if (root == null || root.getPublishedSnapshot() == null || root.getStatus() == PublicationStatus.ARCHIVED) {
            return null;
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("original", root.getRootOriginal());
        map.put("normalized", root.getPublishedNormalized());
        map.put("slug", root.getSlug());
        return map;
    }

    private Map<String, Object> attributionMap(CitationView citation) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("title", citation.title());
        map.put("author", citation.author());
        map.put("edition", citation.edition());
        map.put("publicationYear", citation.publicationYear());
        map.put("pageFrom", citation.pageFrom());
        map.put("pageTo", citation.pageTo());
        map.put("volume", citation.volume());
        map.put("locator", citation.sourceLocator());
        map.put("attributionText", citation.attributionText());
        return map;
    }

    private PublicEntry published(LexicalEntryEntity entry) {
        if (entry.getPublishedSnapshot() == null || entry.getStatus() == PublicationStatus.ARCHIVED) {
            throw missingEntry();
        }
        Map<String, Object> snapshot = entry.getPublishedSnapshot();
        return new PublicEntry(
                entry.getId(),
                entry.getSlug(),
                text(snapshot.get("lemmaOriginal")),
                text(snapshot.get("vocalizedForm")),
                text(snapshot.get("partOfSpeech")),
                text(snapshot.get("gender")),
                rootFrom(snapshot.get("root")),
                sensesFrom(snapshot.get("senses")),
                formsFrom(snapshot.get("forms")),
                relationsFrom(snapshot.get("relations")));
    }

    private DictionaryViews.LookupHit hit(LexicalEntryEntity entry) {
        PublicEntry publicEntry = published(entry);
        String shortDefinition = publicEntry.senses().isEmpty() ? null : firstNonBlank(publicEntry.senses().get(0).shortDefinition(), publicEntry.senses().get(0).definition());
        String root = publicEntry.root() == null ? null : publicEntry.root().original();
        return new DictionaryViews.LookupHit(
                entry.getId(),
                entry.getSlug(),
                publicEntry.lemmaOriginal(),
                publicEntry.vocalizedForm(),
                publicEntry.partOfSpeech(),
                root,
                shortDefinition,
                publicEntry.senses().size());
    }

    private AdminEntry assemble(LexicalEntryEntity entry, boolean withChildren) {
        if (!withChildren) {
            return new AdminEntry(entry.getId(), entry.getLemmaOriginal(), entry.getLemmaNormalized(), entry.getVocalizedForm(), entry.getRootId(), entry.getPartOfSpeech().name(), entry.getGender() == null ? null : entry.getGender().name(), entry.getStatus().name(), entry.getSlug(), entry.getVersion(), visible(entry), List.of(), List.of(), List.of());
        }
        List<LexicalSenseEntity> entrySenses = senses.findByLexicalEntryIdOrderByDisplayOrderAsc(entry.getId());
        List<UUID> senseIds = entrySenses.stream().map(LexicalSenseEntity::getId).toList();
        List<UsageExampleEntity> entryExamples = senseIds.isEmpty() ? List.of() : examples.findBySenseIdInOrderByDisplayOrderAsc(senseIds);
        Map<UUID, List<UUID>> citationsBySense = citationIdsBySense(senseIds);
        List<AdminSense> senseViews = entrySenses.stream().map(sense -> new AdminSense(
                sense.getId(),
                sense.getDefinition(),
                sense.getShortDefinition(),
                sense.getUsageLabel() == null ? null : sense.getUsageLabel().name(),
                sense.getDomainLabel() == null ? null : sense.getDomainLabel().name(),
                sense.getDisplayOrder(),
                sense.getStatus().name(),
                sense.getVersion(),
                citationsBySense.getOrDefault(sense.getId(), List.of()),
                entryExamples.stream().filter(example -> example.getSenseId().equals(sense.getId())).map(example -> new AdminExample(example.getId(), example.getExampleKind().name(), example.getTextOriginal(), example.getExplanation(), example.getCitationId(), example.getDisplayOrder(), example.getStatus().name())).toList())).toList();
        List<AdminForm> formViews = forms.findByLexicalEntryIdOrderByDisplayOrderAsc(entry.getId()).stream()
                .map(form -> new AdminForm(form.getId(), form.getFormType().name(), form.getOriginalForm(), form.getNormalizedForm(), form.getNotes(), form.getDisplayOrder(), form.getStatus().name()))
                .toList();
        List<AdminRelation> relationViews = relations.findTouching(entry.getId()).stream()
                .map(relation -> new AdminRelation(relation.getId(), relation.getRelationType().name(), relation.getSourceEntryId(), relation.getTargetEntryId(), relation.getSourceSenseId(), relation.getTargetSenseId(), relation.getVerificationLevel().name(), relation.getStatus().name()))
                .toList();
        return new AdminEntry(entry.getId(), entry.getLemmaOriginal(), entry.getLemmaNormalized(), entry.getVocalizedForm(), entry.getRootId(), entry.getPartOfSpeech().name(), entry.getGender() == null ? null : entry.getGender().name(), entry.getStatus().name(), entry.getSlug(), entry.getVersion(), visible(entry), senseViews, formViews, relationViews);
    }

    private void applyIdentity(LexicalEntryEntity entry, EntryDraft draft, ArabicText lemma) {
        if (draft.partOfSpeech() == null) {
            throw invalid("partOfSpeech", "Choose a part of speech.");
        }
        if (draft.vocalizedForm() != null && !draft.vocalizedForm().isBlank()) {
            ArabicLexicalText.requireLemma(normalize(draft.vocalizedForm(), "vocalizedForm").normalizedText());
            entry.setVocalizedForm(draft.vocalizedForm().trim());
        } else {
            entry.setVocalizedForm(null);
        }
        if (draft.rootId() != null && roots.findById(draft.rootId()).isEmpty()) {
            throw new ResourceNotFoundException("Root was not found.");
        }
        entry.setRootId(draft.rootId());
        entry.setPartOfSpeech(draft.partOfSpeech());
        entry.setGender(draft.gender());
        if (lemma.normalizedText().isBlank()) {
            throw invalid("lemma", "Enter an Arabic lemma.");
        }
    }

    private void fillSense(LexicalSenseEntity sense, SenseDraft draft, int fallbackOrder) {
        if (draft.definition() == null || draft.definition().isBlank() || draft.definition().length() > 4000) {
            throw invalid("definition", "Enter a definition of at most 4000 characters.");
        }
        if (draft.shortDefinition() != null && draft.shortDefinition().length() > 280) {
            throw invalid("shortDefinition", "The short definition must be at most 280 characters.");
        }
        sense.setDefinition(draft.definition().trim());
        sense.setShortDefinition(blank(draft.shortDefinition()));
        sense.setUsageLabel(draft.usageLabel());
        sense.setDomainLabel(draft.domainLabel());
        sense.setDisplayOrder(draft.displayOrder() == null ? fallbackOrder : draft.displayOrder());
        if (sense.getDisplayOrder() < 1) {
            throw invalid("displayOrder", "Meaning order starts at 1.");
        }
    }

    private void openForEdit(LexicalEntryEntity entry, UUID actor, String reason) {
        if (!EditorialWorkflow.editable(entry.getStatus())) {
            throw new ConflictException("This entry cannot be edited while it is " + entry.getStatus() + ".");
        }
        if (entry.getStatus() == PublicationStatus.PUBLISHED) {
            revisions.record("lexical_entry", entry.getId(), Map.of("lemma", entry.getLemmaOriginal(), "status", entry.getStatus().name()), actor, reason);
            entry.setStatus(PublicationStatus.DRAFT);
            entry.setReviewedBy(null);
        }
    }

    private void requireCitation(UUID citationId) {
        if (sources.citations(List.of(citationId)).isEmpty()) {
            throw new ResourceNotFoundException("Citation was not found.");
        }
    }

    private LexicalEntryEntity locked(UUID id, long version) {
        LexicalEntryEntity entry = entries.lockById(id).orElseThrow(this::missingEntry);
        if (entry.getVersion() != version) {
            throw new ConflictException("The record was updated by someone else. Reload and try again.");
        }
        return entry;
    }

    private void stamp(LexicalEntryEntity entry, UUID actor, boolean creating) {
        Instant now = timeProvider.now();
        if (creating) {
            entry.setCreatedAt(now);
            entry.setCreatedBy(actor);
        }
        entry.setUpdatedAt(now);
        entry.setUpdatedBy(actor);
    }

    private void stampSense(LexicalSenseEntity sense, UUID actor, boolean creating) {
        Instant now = timeProvider.now();
        if (creating) {
            sense.setCreatedAt(now);
            sense.setCreatedBy(actor);
        }
        sense.setUpdatedAt(now);
        sense.setUpdatedBy(actor);
    }

    private void touch(LexicalEntryEntity entry, UUID actor) {
        entry.setUpdatedAt(timeProvider.now());
        entry.setUpdatedBy(actor);
    }

    private ArabicText normalize(String value, String field) {
        if (value == null || value.isBlank()) {
            throw invalid(field, "This text is required.");
        }
        if (value.codePointCount(0, value.length()) > 200) {
            throw invalid(field, "This text is too long.");
        }
        return text.normalize(value.trim());
    }

    private Map<UUID, List<UUID>> citationIdsBySense(List<UUID> senseIds) {
        Map<UUID, List<UUID>> grouped = new LinkedHashMap<>();
        if (senseIds.isEmpty()) {
            return grouped;
        }
        for (SenseCitationEntity link : senseCitations.findByOwnerIdIn(senseIds)) {
            grouped.computeIfAbsent(link.getOwnerId(), ignored -> new ArrayList<>()).add(link.getCitationId());
        }
        return grouped;
    }

    private static boolean visible(LexicalEntryEntity entry) {
        return entry.getPublishedSnapshot() != null && entry.getStatus() != PublicationStatus.ARCHIVED;
    }

    private static String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private static String firstNonBlank(String preferred, String fallback) {
        return preferred == null || preferred.isBlank() ? fallback : preferred;
    }

    @SuppressWarnings("unchecked")
    private PublicRootRef rootFrom(Object value) {
        if (!(value instanceof Map<?, ?> map)) {
            return null;
        }
        return new PublicRootRef(text(map.get("original")), text(map.get("normalized")), text(map.get("slug")));
    }

    @SuppressWarnings("unchecked")
    private List<PublicSense> sensesFrom(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<PublicSense> result = new ArrayList<>();
        for (Object item : list) {
            Map<String, Object> map = (Map<String, Object>) item;
            result.add(new PublicSense(
                    text(map.get("definition")),
                    text(map.get("shortDefinition")),
                    text(map.get("usageLabel")),
                    text(map.get("domainLabel")),
                    number(map.get("displayOrder")),
                    examplesFrom(map.get("examples")),
                    attributionsFrom(map.get("sources"))));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<PublicExample> examplesFrom(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<PublicExample> result = new ArrayList<>();
        for (Object item : list) {
            Map<String, Object> map = (Map<String, Object>) item;
            result.add(new PublicExample(text(map.get("textOriginal")), text(map.get("explanation")), text(map.get("kind"))));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<Attribution> attributionsFrom(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<Attribution> result = new ArrayList<>();
        for (Object item : list) {
            Map<String, Object> map = (Map<String, Object>) item;
            result.add(new Attribution(text(map.get("title")), text(map.get("author")), text(map.get("edition")), integer(map.get("publicationYear")), integer(map.get("pageFrom")), integer(map.get("pageTo")), text(map.get("volume")), text(map.get("locator")), text(map.get("attributionText"))));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<PublicForm> formsFrom(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<PublicForm> result = new ArrayList<>();
        for (Object item : list) {
            Map<String, Object> map = (Map<String, Object>) item;
            result.add(new PublicForm(text(map.get("formType")), text(map.get("originalForm"))));
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<PublicRelation> relationsFrom(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<PublicRelation> result = new ArrayList<>();
        for (Object item : list) {
            Map<String, Object> map = (Map<String, Object>) item;
            result.add(new PublicRelation(text(map.get("relationType")), text(map.get("otherLemma")), text(map.get("otherSlug")), text(map.get("direction"))));
        }
        return result;
    }

    private static int number(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }

    private static Integer integer(Object value) {
        return value instanceof Number number ? number.intValue() : null;
    }

    private static int bound(int page, int size) {
        if (page < 0 || size < 1 || size > 50) {
            throw new ValidationException("Page request is invalid.", List.of(new FieldErrorDetail("page", "Page size must be from 1 to 50.")));
        }
        return size;
    }

    private static ValidationException invalid(String field, String message) {
        return new ValidationException(message, List.of(new FieldErrorDetail(field, message)));
    }

    private ResourceNotFoundException missingEntry() {
        return new ResourceNotFoundException("Dictionary entry was not found.");
    }
}
