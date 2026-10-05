package com.mrsoft.arabicreference.spelling.infrastructure.persistence;

import com.mrsoft.arabicreference.linguistics.infrastructure.persistence.EditorialEntity;
import com.mrsoft.arabicreference.spelling.domain.SpellingDifficulty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "spelling_rule")
public class SpellingRuleEntity extends EditorialEntity {

    @Column(name = "topic_id", nullable = false)
    private UUID topicId;

    @Column(name = "title_original", nullable = false, length = 160)
    private String titleOriginal;

    @Column(name = "title_normalized", nullable = false, length = 160)
    private String titleNormalized;

    @Column(nullable = false, length = 180)
    private String slug;

    @Column(length = 1000)
    private String summary;

    @Column(name = "core_rule", nullable = false, length = 4000)
    private String coreRule;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SpellingDifficulty difficulty;

    public UUID getTopicId() { return topicId; }
    public void setTopicId(UUID topicId) { this.topicId = topicId; }
    public String getTitleOriginal() { return titleOriginal; }
    public void setTitleOriginal(String titleOriginal) { this.titleOriginal = titleOriginal; }
    public String getTitleNormalized() { return titleNormalized; }
    public void setTitleNormalized(String titleNormalized) { this.titleNormalized = titleNormalized; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getCoreRule() { return coreRule; }
    public void setCoreRule(String coreRule) { this.coreRule = coreRule; }
    public SpellingDifficulty getDifficulty() { return difficulty; }
    public void setDifficulty(SpellingDifficulty difficulty) { this.difficulty = difficulty; }
}
