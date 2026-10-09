package com.mrsoft.arabicreference.contentimport.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "content_import_batch")
public class ContentImportBatchEntity {
    @Id
    private UUID id;

    @Column(name = "source_id", nullable = false)
    private UUID sourceId;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_sha256", nullable = false, length = 64)
    private String fileSha256;

    @Column(name = "imported_at", nullable = false)
    private Instant importedAt;

    @Column(name = "records_read", nullable = false)
    private int recordsRead;

    @Column(name = "valid_count", nullable = false)
    private int validCount;

    @Column(name = "invalid_count", nullable = false)
    private int invalidCount;

    @Column(name = "duplicate_count", nullable = false)
    private int duplicateCount;

    @Column(name = "new_count", nullable = false)
    private int newCount;

    @Column(name = "update_count", nullable = false)
    private int updateCount;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getSourceId() { return sourceId; }
    public void setSourceId(UUID sourceId) { this.sourceId = sourceId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getFileSha256() { return fileSha256; }
    public void setFileSha256(String fileSha256) { this.fileSha256 = fileSha256; }
    public Instant getImportedAt() { return importedAt; }
    public void setImportedAt(Instant importedAt) { this.importedAt = importedAt; }
    public int getRecordsRead() { return recordsRead; }
    public void setRecordsRead(int recordsRead) { this.recordsRead = recordsRead; }
    public int getValidCount() { return validCount; }
    public void setValidCount(int validCount) { this.validCount = validCount; }
    public int getInvalidCount() { return invalidCount; }
    public void setInvalidCount(int invalidCount) { this.invalidCount = invalidCount; }
    public int getDuplicateCount() { return duplicateCount; }
    public void setDuplicateCount(int duplicateCount) { this.duplicateCount = duplicateCount; }
    public int getNewCount() { return newCount; }
    public void setNewCount(int newCount) { this.newCount = newCount; }
    public int getUpdateCount() { return updateCount; }
    public void setUpdateCount(int updateCount) { this.updateCount = updateCount; }
    public UUID getCreatedBy() { return createdBy; }
    public void setCreatedBy(UUID createdBy) { this.createdBy = createdBy; }
}
