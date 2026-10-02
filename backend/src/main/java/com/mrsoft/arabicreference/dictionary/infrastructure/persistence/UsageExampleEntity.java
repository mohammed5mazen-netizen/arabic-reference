package com.mrsoft.arabicreference.dictionary.infrastructure.persistence;

import com.mrsoft.arabicreference.dictionary.domain.ExampleKind;
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
@Table(name = "usage_example")
public class UsageExampleEntity {

    @Id
    private UUID id;

    @Column(name = "sense_id", nullable = false)
    private UUID senseId;

    @Enumerated(EnumType.STRING)
    @Column(name = "example_kind", nullable = false, length = 32)
    private ExampleKind exampleKind;

    @Column(name = "text_original", nullable = false, length = 1000)
    private String textOriginal;

    @Column(name = "text_normalized", nullable = false, length = 1000)
    private String textNormalized;

    @Column(length = 1000)
    private String explanation;

    @Column(name = "citation_id")
    private UUID citationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PublicationStatus status;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

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
    public UUID getSenseId() { return senseId; }
    public void setSenseId(UUID senseId) { this.senseId = senseId; }
    public ExampleKind getExampleKind() { return exampleKind; }
    public void setExampleKind(ExampleKind exampleKind) { this.exampleKind = exampleKind; }
    public String getTextOriginal() { return textOriginal; }
    public void setTextOriginal(String textOriginal) { this.textOriginal = textOriginal; }
    public String getTextNormalized() { return textNormalized; }
    public void setTextNormalized(String textNormalized) { this.textNormalized = textNormalized; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public UUID getCitationId() { return citationId; }
    public void setCitationId(UUID citationId) { this.citationId = citationId; }
    public PublicationStatus getStatus() { return status; }
    public void setStatus(PublicationStatus status) { this.status = status; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
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
