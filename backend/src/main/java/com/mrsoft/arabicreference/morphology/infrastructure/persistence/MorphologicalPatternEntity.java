package com.mrsoft.arabicreference.morphology.infrastructure.persistence;

import com.mrsoft.arabicreference.morphology.domain.PatternCategory;
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
@Table(name = "morphological_pattern")
public class MorphologicalPatternEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 40)
    private String code;

    @Column(name = "pattern_original", nullable = false, length = 40)
    private String patternOriginal;

    @Column(name = "pattern_normalized", nullable = false, length = 40)
    private String patternNormalized;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private PatternCategory category;

    @Column(name = "radical_count", nullable = false)
    private short radicalCount;

    @Column(length = 300)
    private String description;

    @Column(nullable = false, length = 16)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private long version;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getPatternOriginal() { return patternOriginal; }
    public void setPatternOriginal(String patternOriginal) { this.patternOriginal = patternOriginal; }
    public String getPatternNormalized() { return patternNormalized; }
    public void setPatternNormalized(String patternNormalized) { this.patternNormalized = patternNormalized; }
    public PatternCategory getCategory() { return category; }
    public void setCategory(PatternCategory category) { this.category = category; }
    public short getRadicalCount() { return radicalCount; }
    public void setRadicalCount(short radicalCount) { this.radicalCount = radicalCount; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
