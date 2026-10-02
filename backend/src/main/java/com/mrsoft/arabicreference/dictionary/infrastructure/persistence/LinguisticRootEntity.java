package com.mrsoft.arabicreference.dictionary.infrastructure.persistence;

import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "linguistic_root")
public class LinguisticRootEntity {

    @Id
    private UUID id;

    @Column(name = "root_original", nullable = false, length = 32)
    private String rootOriginal;

    @Column(name = "root_normalized", nullable = false, length = 32)
    private String rootNormalized;

    @Column(name = "radical_count", nullable = false)
    private short radicalCount;

    @Column(length = 500)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PublicationStatus status;

    @Column(nullable = false, length = 80)
    private String slug;

    @Column(name = "published_normalized", length = 32)
    private String publishedNormalized;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "published_snapshot", columnDefinition = "jsonb")
    private Map<String, Object> publishedSnapshot;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "change_reason", length = 500)
    private String changeReason;

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
    public String getRootOriginal() { return rootOriginal; }
    public void setRootOriginal(String rootOriginal) { this.rootOriginal = rootOriginal; }
    public String getRootNormalized() { return rootNormalized; }
    public void setRootNormalized(String rootNormalized) { this.rootNormalized = rootNormalized; }
    public short getRadicalCount() { return radicalCount; }
    public void setRadicalCount(short radicalCount) { this.radicalCount = radicalCount; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public PublicationStatus getStatus() { return status; }
    public void setStatus(PublicationStatus status) { this.status = status; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getPublishedNormalized() { return publishedNormalized; }
    public void setPublishedNormalized(String publishedNormalized) { this.publishedNormalized = publishedNormalized; }
    public Map<String, Object> getPublishedSnapshot() { return publishedSnapshot; }
    public void setPublishedSnapshot(Map<String, Object> publishedSnapshot) { this.publishedSnapshot = publishedSnapshot; }
    public UUID getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(UUID reviewedBy) { this.reviewedBy = reviewedBy; }
    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }
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
