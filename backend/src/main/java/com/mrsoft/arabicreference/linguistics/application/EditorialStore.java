package com.mrsoft.arabicreference.linguistics.application;

import com.mrsoft.arabicreference.identity.application.AuditRecorder;
import com.mrsoft.arabicreference.identity.domain.AuditEventType;
import com.mrsoft.arabicreference.linguistics.domain.editorial.EditorialGuards;
import com.mrsoft.arabicreference.linguistics.domain.editorial.EditorialWorkflow;
import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import com.mrsoft.arabicreference.linguistics.infrastructure.persistence.EditorialEntity;
import com.mrsoft.arabicreference.shared.kernel.exception.ConflictException;
import com.mrsoft.arabicreference.shared.kernel.id.Ids;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import jakarta.persistence.EntityManager;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

@Service
public class EditorialStore {

    private final EntityManager entityManager;
    private final ContentRevisionRecorder revisions;
    private final AuditRecorder audit;
    private final TimeProvider time;

    public EditorialStore(EntityManager entityManager, ContentRevisionRecorder revisions, AuditRecorder audit, TimeProvider time) {
        this.entityManager = entityManager;
        this.revisions = revisions;
        this.audit = audit;
        this.time = time;
    }

    public void prepareNew(EditorialEntity record, UUID actor) {
        var now = time.now();
        record.setId(Ids.random());
        record.setStatus(PublicationStatus.DRAFT);
        record.setVersion(null);
        record.setCreatedAt(now);
        record.setUpdatedAt(now);
        record.setCreatedBy(actor);
        record.setUpdatedBy(actor);
    }

    public void requireVersion(EditorialEntity record, long version) {
        if (record.getVersion() != version) {
            throw new ConflictException("The record was updated by someone else. Reload and try again.");
        }
    }

    public void open(EditorialEntity record, UUID actor, String reason, String targetType) {
        if (!EditorialWorkflow.editable(record.getStatus())) {
            throw new ConflictException("This record cannot be edited while it is " + record.getStatus() + ".");
        }
        if (record.getStatus() == PublicationStatus.PUBLISHED) {
            Map<String, Object> snapshot = record.getPublishedSnapshot() == null
                    ? Map.of("status", "PUBLISHED")
                    : new LinkedHashMap<>(record.getPublishedSnapshot());
            revisions.record(targetType, record.getId(), snapshot, actor, reason);
            record.setStatus(PublicationStatus.DRAFT);
            record.setReviewedBy(null);
        }
    }

    public void review(EditorialEntity record, UUID actor) {
        EditorialGuards.requireDifferentPerson(record.getCreatedBy(), actor, "The creator cannot review their own content.");
        record.setStatus(EditorialWorkflow.verify(record.getStatus()));
        record.setReviewedBy(actor);
    }

    public void requestChanges(EditorialEntity record, UUID actor, String reason) {
        EditorialGuards.requireDifferentPerson(record.getCreatedBy(), actor, "The creator cannot review their own content.");
        record.setStatus(EditorialWorkflow.requestChanges(record.getStatus()));
        record.setReviewedBy(actor);
        record.setChangeReason(reason);
    }

    public void submit(EditorialEntity record) {
        record.setStatus(EditorialWorkflow.submit(record.getStatus()));
        record.setChangeReason(null);
    }

    public void publish(EditorialEntity record, UUID actor, String contentType) {
        PublicationChecks.assertNoOpenBlocker(contentType, record.getId());
        EditorialGuards.requireDifferentPerson(record.getCreatedBy(), actor, "The creator cannot publish their own content.");
        EditorialGuards.requireDifferentPerson(record.getReviewedBy(), actor, "The reviewer cannot publish the same content.");
        record.setStatus(EditorialWorkflow.publish(record.getStatus()));
    }

    public void archive(EditorialEntity record) {
        record.setStatus(EditorialWorkflow.archive(record.getStatus()));
    }

    public <T extends EditorialEntity> T persist(JpaRepository<T, UUID> repository, T record, UUID actor, AuditEventType event, String targetType) {
        record.setUpdatedAt(time.now());
        record.setUpdatedBy(actor);
        T persisted = repository.saveAndFlush(record);
        if (!entityManager.contains(persisted)) {
            @SuppressWarnings("unchecked")
            Class<T> type = (Class<T>) record.getClass();
            persisted = entityManager.find(type, record.getId());
        }
        entityManager.refresh(persisted);
        if (persisted != record) {
            record.setVersion(persisted.getVersion());
            record.setStatus(persisted.getStatus());
        }
        audit.record(actor, event, targetType, record.getId().toString(), Map.of("status", record.getStatus().name()));
        return record;
    }
}
