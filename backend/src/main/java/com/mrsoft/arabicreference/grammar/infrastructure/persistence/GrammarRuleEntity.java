package com.mrsoft.arabicreference.grammar.infrastructure.persistence;

import com.mrsoft.arabicreference.grammar.domain.DifficultyLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "grammar_rule")
public class GrammarRuleEntity extends EditorialRecord {

    @Column(name = "topic_id", nullable = false)
    private UUID topicId;

    @Column(name = "title_original", nullable = false, length = 160)
    private String titleOriginal;

    @Column(name = "title_normalized", nullable = false, length = 160)
    private String titleNormalized;

    @Column(name = "published_title", length = 160)
    private String publishedTitle;

    @Column(name = "published_normalized", length = 160)
    private String publishedNormalized;

    @Column(length = 1000)
    private String summary;

    @Column(name = "published_summary", length = 1000)
    private String publishedSummary;

    @Column(nullable = false, length = 180)
    private String slug;

    @Column(name = "rule_text", length = 4000)
    private String ruleText;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "published_display_order")
    private Integer publishedDisplayOrder;

    @Column(name = "published_topic_id")
    private UUID publishedTopicId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DifficultyLevel difficulty;

    public UUID getTopicId() { return topicId; }
    public void setTopicId(UUID topicId) { this.topicId = topicId; }
    public String getTitleOriginal() { return titleOriginal; }
    public void setTitleOriginal(String titleOriginal) { this.titleOriginal = titleOriginal; }
    public String getTitleNormalized() { return titleNormalized; }
    public void setTitleNormalized(String titleNormalized) { this.titleNormalized = titleNormalized; }
    public String getPublishedTitle() { return publishedTitle; }
    public void setPublishedTitle(String publishedTitle) { this.publishedTitle = publishedTitle; }
    public String getPublishedNormalized() { return publishedNormalized; }
    public void setPublishedNormalized(String publishedNormalized) { this.publishedNormalized = publishedNormalized; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getPublishedSummary() { return publishedSummary; }
    public void setPublishedSummary(String publishedSummary) { this.publishedSummary = publishedSummary; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getRuleText() { return ruleText; }
    public void setRuleText(String ruleText) { this.ruleText = ruleText; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    public Integer getPublishedDisplayOrder() { return publishedDisplayOrder; }
    public void setPublishedDisplayOrder(Integer publishedDisplayOrder) { this.publishedDisplayOrder = publishedDisplayOrder; }
    public UUID getPublishedTopicId() { return publishedTopicId; }
    public void setPublishedTopicId(UUID publishedTopicId) { this.publishedTopicId = publishedTopicId; }
    public DifficultyLevel getDifficulty() { return difficulty; }
    public void setDifficulty(DifficultyLevel difficulty) { this.difficulty = difficulty; }
}
