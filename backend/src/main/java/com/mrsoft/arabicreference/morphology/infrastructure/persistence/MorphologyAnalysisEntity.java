package com.mrsoft.arabicreference.morphology.infrastructure.persistence;

import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import com.mrsoft.arabicreference.morphology.domain.DerivationKind;
import com.mrsoft.arabicreference.morphology.domain.StemVowel;
import com.mrsoft.arabicreference.morphology.domain.VerbClass;
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
@Table(name = "morphology_analysis")
public class MorphologyAnalysisEntity {

    @Id
    private UUID id;

    @Column(name = "lexical_entry_id", nullable = false)
    private UUID lexicalEntryId;

    @Column(name = "pattern_id")
    private UUID patternId;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private DerivationKind derivation;

    @Enumerated(EnumType.STRING)
    @Column(name = "verb_class", length = 32)
    private VerbClass verbClass;

    @Enumerated(EnumType.STRING)
    @Column(name = "imperfect_vowel", length = 16)
    private StemVowel imperfectVowel;

    @Column(length = 500)
    private String notes;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> features;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> segmentation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PublicationStatus status;

    @Column(name = "reviewed_by")
    private UUID reviewedBy;

    @Column(name = "change_reason", length = 500)
    private String changeReason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "published_snapshot", columnDefinition = "jsonb")
    private Map<String, Object> publishedSnapshot;

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
    public UUID getLexicalEntryId() { return lexicalEntryId; }
    public void setLexicalEntryId(UUID lexicalEntryId) { this.lexicalEntryId = lexicalEntryId; }
    public UUID getPatternId() { return patternId; }
    public void setPatternId(UUID patternId) { this.patternId = patternId; }
    public DerivationKind getDerivation() { return derivation; }
    public void setDerivation(DerivationKind derivation) { this.derivation = derivation; }
    public VerbClass getVerbClass() { return verbClass; }
    public void setVerbClass(VerbClass verbClass) { this.verbClass = verbClass; }
    public StemVowel getImperfectVowel() { return imperfectVowel; }
    public void setImperfectVowel(StemVowel imperfectVowel) { this.imperfectVowel = imperfectVowel; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Map<String, Object> getFeatures() { return features; }
    public void setFeatures(Map<String, Object> features) { this.features = features; }
    public Map<String, Object> getSegmentation() { return segmentation; }
    public void setSegmentation(Map<String, Object> segmentation) { this.segmentation = segmentation; }
    public PublicationStatus getStatus() { return status; }
    public void setStatus(PublicationStatus status) { this.status = status; }
    public UUID getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(UUID reviewedBy) { this.reviewedBy = reviewedBy; }
    public String getChangeReason() { return changeReason; }
    public void setChangeReason(String changeReason) { this.changeReason = changeReason; }
    public Map<String, Object> getPublishedSnapshot() { return publishedSnapshot; }
    public void setPublishedSnapshot(Map<String, Object> publishedSnapshot) { this.publishedSnapshot = publishedSnapshot; }
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
