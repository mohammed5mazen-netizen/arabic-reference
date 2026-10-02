package com.mrsoft.arabicreference.linguistics.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "content_revision")
public class ContentRevisionEntity {

    @Id
    private UUID id;

    @Column(name = "target_type", nullable = false, length = 40)
    private String targetType;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Column(name = "revision_number", nullable = false)
    private int revisionNumber;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> snapshot;

    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    @Column(name = "change_reason", length = 500)
    private String changeReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public UUID getTargetId() { return targetId; }
    public void setTargetId(UUID targetId) { this.targetId = targetId; }
    public int getRevisionNumber() { return revisionNumber; }
    public void setRevisionNumber(int revisionNumber) { this.revisionNumber = revisionNumber; }
    public Map<String, Object> getSnapshot() { return snapshot; }
    public void setSnapshot(Map<String, Object> snapshot) { this.snapshot = snapshot; }
    public UUID getActorId() { return actorId; }
    public void setActorId(UUID actorId) { this.actorId = actorId; }
    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
