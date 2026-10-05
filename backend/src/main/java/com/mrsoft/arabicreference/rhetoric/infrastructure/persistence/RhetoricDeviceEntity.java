package com.mrsoft.arabicreference.rhetoric.infrastructure.persistence;

import com.mrsoft.arabicreference.linguistics.infrastructure.persistence.EditorialEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "rhetoric_device")
public class RhetoricDeviceEntity extends EditorialEntity {
    @Column(name = "topic_id", nullable = false)
    private UUID topicId;
    @Column(name = "name_original", nullable = false, length = 160)
    private String nameOriginal;
    @Column(name = "name_normalized", nullable = false, length = 160)
    private String nameNormalized;
    @Column(nullable = false, length = 180)
    private String slug;
    @Column(name = "short_definition", nullable = false, length = 500)
    private String shortDefinition;
    @Column(name = "detailed_explanation", length = 4000)
    private String detailedExplanation;

    public UUID getTopicId() { return topicId; }
    public void setTopicId(UUID topicId) { this.topicId = topicId; }
    public String getNameOriginal() { return nameOriginal; }
    public void setNameOriginal(String nameOriginal) { this.nameOriginal = nameOriginal; }
    public String getNameNormalized() { return nameNormalized; }
    public void setNameNormalized(String nameNormalized) { this.nameNormalized = nameNormalized; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getShortDefinition() { return shortDefinition; }
    public void setShortDefinition(String shortDefinition) { this.shortDefinition = shortDefinition; }
    public String getDetailedExplanation() { return detailedExplanation; }
    public void setDetailedExplanation(String detailedExplanation) { this.detailedExplanation = detailedExplanation; }
}
