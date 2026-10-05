package com.mrsoft.arabicreference.grammar.infrastructure.persistence;

import com.mrsoft.arabicreference.grammar.domain.ExampleType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "grammar_example")
public class GrammarExampleEntity {

    @Id
    private UUID id;

    @Column(name = "rule_id", nullable = false)
    private UUID ruleId;

    @Column(name = "component_id")
    private UUID componentId;

    @Column(name = "text_original", nullable = false, length = 1000)
    private String textOriginal;

    @Column(name = "text_normalized", nullable = false, length = 1000)
    private String textNormalized;

    @Column(length = 1000)
    private String explanation;

    @Enumerated(EnumType.STRING)
    @Column(name = "example_type", nullable = false, length = 32)
    private ExampleType exampleType;

    @Column(name = "citation_id")
    private UUID citationId;

    private Integer surah;
    private Integer ayah;

    @Column(length = 160)
    private String poet;

    @Column(name = "work_title", length = 160)
    private String workTitle;

    @Column(name = "verse_locator", length = 80)
    private String verseLocator;

    @Column(name = "annotation_id")
    private UUID annotationId;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getRuleId() { return ruleId; }
    public void setRuleId(UUID ruleId) { this.ruleId = ruleId; }
    public UUID getComponentId() { return componentId; }
    public void setComponentId(UUID componentId) { this.componentId = componentId; }
    public String getTextOriginal() { return textOriginal; }
    public void setTextOriginal(String textOriginal) { this.textOriginal = textOriginal; }
    public String getTextNormalized() { return textNormalized; }
    public void setTextNormalized(String textNormalized) { this.textNormalized = textNormalized; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public ExampleType getExampleType() { return exampleType; }
    public void setExampleType(ExampleType exampleType) { this.exampleType = exampleType; }
    public UUID getCitationId() { return citationId; }
    public void setCitationId(UUID citationId) { this.citationId = citationId; }
    public Integer getSurah() { return surah; }
    public void setSurah(Integer surah) { this.surah = surah; }
    public Integer getAyah() { return ayah; }
    public void setAyah(Integer ayah) { this.ayah = ayah; }
    public String getPoet() { return poet; }
    public void setPoet(String poet) { this.poet = poet; }
    public String getWorkTitle() { return workTitle; }
    public void setWorkTitle(String workTitle) { this.workTitle = workTitle; }
    public String getVerseLocator() { return verseLocator; }
    public void setVerseLocator(String verseLocator) { this.verseLocator = verseLocator; }
    public UUID getAnnotationId() { return annotationId; }
    public void setAnnotationId(UUID annotationId) { this.annotationId = annotationId; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
}
