package com.mrsoft.arabicreference.dictionary.infrastructure.persistence;

import com.mrsoft.arabicreference.dictionary.domain.RelationType;
import com.mrsoft.arabicreference.dictionary.domain.VerificationLevel;
import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "linguistic_relation")
public class LinguisticRelationEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type", nullable = false, length = 32)
    private RelationType relationType;

    @Column(name = "source_entry_id", nullable = false)
    private UUID sourceEntryId;

    @Column(name = "target_entry_id", nullable = false)
    private UUID targetEntryId;

    @Column(name = "source_sense_id")
    private UUID sourceSenseId;

    @Column(name = "target_sense_id")
    private UUID targetSenseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_level", nullable = false, length = 32)
    private VerificationLevel verificationLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PublicationStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "updated_by", nullable = false)
    private UUID updatedBy;

    @Version
    @Column(nullable = false)
    private long version;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public RelationType getRelationType() { return relationType; }
    public void setRelationType(RelationType relationType) { this.relationType = relationType; }
    public UUID getSourceEntryId() { return sourceEntryId; }
    public void setSourceEntryId(UUID sourceEntryId) { this.sourceEntryId = sourceEntryId; }
    public UUID getTargetEntryId() { return targetEntryId; }
    public void setTargetEntryId(UUID targetEntryId) { this.targetEntryId = targetEntryId; }
    public UUID getSourceSenseId() { return sourceSenseId; }
    public void setSourceSenseId(UUID sourceSenseId) { this.sourceSenseId = sourceSenseId; }
    public UUID getTargetSenseId() { return targetSenseId; }
    public void setTargetSenseId(UUID targetSenseId) { this.targetSenseId = targetSenseId; }
    public VerificationLevel getVerificationLevel() { return verificationLevel; }
    public void setVerificationLevel(VerificationLevel verificationLevel) { this.verificationLevel = verificationLevel; }
    public PublicationStatus getStatus() { return status; }
    public void setStatus(PublicationStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
    public UUID getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(UUID updatedBy) { this.updatedBy = updatedBy; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
