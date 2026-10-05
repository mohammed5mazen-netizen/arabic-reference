package com.mrsoft.arabicreference.rhetoric.infrastructure.persistence;

import com.mrsoft.arabicreference.rhetoric.domain.RhetoricCategory;
import com.mrsoft.arabicreference.linguistics.infrastructure.persistence.EditorialEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "rhetoric_topic")
public class RhetoricTopicEntity extends EditorialEntity {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RhetoricCategory category;
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

    public RhetoricCategory getCategory() { return category; }
    public void setCategory(RhetoricCategory category) { this.category = category; }
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
