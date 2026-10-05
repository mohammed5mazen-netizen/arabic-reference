package com.mrsoft.arabicreference.literature.infrastructure.persistence;

import com.mrsoft.arabicreference.linguistics.infrastructure.persistence.EditorialEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "literary_era")
public class LiteraryEraEntity extends EditorialEntity {

    @Column(name = "name_original", nullable = false, length = 160)
    private String nameOriginal;

    @Column(name = "name_normalized", nullable = false, length = 160)
    private String nameNormalized;

    @Column(nullable = false, length = 180)
    private String slug;

    @Column(name = "start_description", length = 300)
    private String startDescription;

    @Column(name = "end_description", length = 300)
    private String endDescription;

    @Column(length = 2000)
    private String summary;

    @Column(name = "historical_context", length = 4000)
    private String historicalContext;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    public String getNameOriginal() { return nameOriginal; }
    public void setNameOriginal(String nameOriginal) { this.nameOriginal = nameOriginal; }
    public String getNameNormalized() { return nameNormalized; }
    public void setNameNormalized(String nameNormalized) { this.nameNormalized = nameNormalized; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getStartDescription() { return startDescription; }
    public void setStartDescription(String startDescription) { this.startDescription = startDescription; }
    public String getEndDescription() { return endDescription; }
    public void setEndDescription(String endDescription) { this.endDescription = endDescription; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getHistoricalContext() { return historicalContext; }
    public void setHistoricalContext(String historicalContext) { this.historicalContext = historicalContext; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
}
