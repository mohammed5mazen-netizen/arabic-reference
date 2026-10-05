package com.mrsoft.arabicreference.grammar.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "grammar_concept")
public class GrammarConceptEntity extends EditorialRecord {

    @Column(name = "term_original", nullable = false, length = 160)
    private String termOriginal;

    @Column(name = "term_normalized", nullable = false, length = 160)
    private String termNormalized;

    @Column(name = "published_title", length = 160)
    private String publishedTitle;

    @Column(name = "published_normalized", length = 160)
    private String publishedNormalized;

    @Column(nullable = false, length = 180)
    private String slug;

    @Column(name = "short_definition", length = 500)
    private String shortDefinition;

    @Column(name = "published_summary", length = 500)
    private String publishedSummary;

    @Column(name = "detailed_definition", length = 4000)
    private String detailedDefinition;

    public String getTermOriginal() { return termOriginal; }
    public void setTermOriginal(String termOriginal) { this.termOriginal = termOriginal; }
    public String getTermNormalized() { return termNormalized; }
    public void setTermNormalized(String termNormalized) { this.termNormalized = termNormalized; }
    public String getPublishedTitle() { return publishedTitle; }
    public void setPublishedTitle(String publishedTitle) { this.publishedTitle = publishedTitle; }
    public String getPublishedNormalized() { return publishedNormalized; }
    public void setPublishedNormalized(String publishedNormalized) { this.publishedNormalized = publishedNormalized; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
    public String getShortDefinition() { return shortDefinition; }
    public void setShortDefinition(String shortDefinition) { this.shortDefinition = shortDefinition; }
    public String getPublishedSummary() { return publishedSummary; }
    public void setPublishedSummary(String publishedSummary) { this.publishedSummary = publishedSummary; }
    public String getDetailedDefinition() { return detailedDefinition; }
    public void setDetailedDefinition(String detailedDefinition) { this.detailedDefinition = detailedDefinition; }
}
