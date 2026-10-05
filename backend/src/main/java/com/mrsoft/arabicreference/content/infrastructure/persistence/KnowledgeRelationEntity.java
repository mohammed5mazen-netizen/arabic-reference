package com.mrsoft.arabicreference.content.infrastructure.persistence;

import com.mrsoft.arabicreference.content.domain.KnowledgeOwnerType;
import com.mrsoft.arabicreference.content.domain.KnowledgeTargetType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "knowledge_relation")
public class KnowledgeRelationEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false, length = 32)
    private KnowledgeOwnerType ownerType;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 32)
    private KnowledgeTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public KnowledgeOwnerType getOwnerType() { return ownerType; }
    public void setOwnerType(KnowledgeOwnerType ownerType) { this.ownerType = ownerType; }
    public UUID getOwnerId() { return ownerId; }
    public void setOwnerId(UUID ownerId) { this.ownerId = ownerId; }
    public KnowledgeTargetType getTargetType() { return targetType; }
    public void setTargetType(KnowledgeTargetType targetType) { this.targetType = targetType; }
    public UUID getTargetId() { return targetId; }
    public void setTargetId(UUID targetId) { this.targetId = targetId; }
}
