package com.mrsoft.arabicreference.spelling.infrastructure.persistence;

import com.mrsoft.arabicreference.linguistics.infrastructure.persistence.EditorialEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "spelling_topic")
public class SpellingTopicEntity extends EditorialEntity {

    @Column(name = "title_original", nullable = false, length = 160)
    private String titleOriginal;

    @Column(name = "title_normalized", nullable = false, length = 160)
    private String titleNormalized;

    @Column(nullable = false, length = 180)
    private String slug;

    @Column(length = 1000)
    private String summary;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    public String getTitleOriginal() { return titleOriginal; }
    public void setTitleOriginal(String titleOriginal) { this.titleOriginal = titleOriginal; }
    public String getTitleNormalized() { return titleNormalized; }
    public void setTitleNormalized(String titleNormalized) { this.titleNormalized = titleNormalized; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
}
