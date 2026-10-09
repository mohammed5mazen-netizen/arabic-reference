package com.mrsoft.arabicreference.contentimport.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "content_import_record")
public class ContentImportRecordEntity {
    @Id
    private UUID id;

    @Column(name = "source_id", nullable = false)
    private UUID sourceId;

    @Column(name = "source_record_key", nullable = false, length = 160)
    private String sourceRecordKey;

    @Column(name = "target_type", nullable = false, length = 40)
    private String targetType;

    @Column(name = "target_id", nullable = false)
    private UUID targetId;

    @Column(name = "citation_id", nullable = false)
    private UUID citationId;

    @Column(name = "content_sha256", nullable = false, length = 64)
    private String contentSha256;

    @Column(name = "last_batch_id", nullable = false)
    private UUID lastBatchId;

    @Column(name = "imported_at", nullable = false)
    private Instant importedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getSourceId() { return sourceId; }
    public void setSourceId(UUID sourceId) { this.sourceId = sourceId; }
    public String getSourceRecordKey() { return sourceRecordKey; }
    public void setSourceRecordKey(String sourceRecordKey) { this.sourceRecordKey = sourceRecordKey; }
    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }
    public UUID getTargetId() { return targetId; }
    public void setTargetId(UUID targetId) { this.targetId = targetId; }
    public UUID getCitationId() { return citationId; }
    public void setCitationId(UUID citationId) { this.citationId = citationId; }
    public String getContentSha256() { return contentSha256; }
    public void setContentSha256(String contentSha256) { this.contentSha256 = contentSha256; }
    public UUID getLastBatchId() { return lastBatchId; }
    public void setLastBatchId(UUID lastBatchId) { this.lastBatchId = lastBatchId; }
    public Instant getImportedAt() { return importedAt; }
    public void setImportedAt(Instant importedAt) { this.importedAt = importedAt; }
}
