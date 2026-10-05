package com.mrsoft.arabicreference.grammar.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "grammar_concept_alias")
public class GrammarConceptAliasEntity {

    @Id
    private UUID id;

    @Column(name = "concept_id", nullable = false)
    private UUID conceptId;

    @Column(name = "alias_original", nullable = false, length = 160)
    private String aliasOriginal;

    @Column(name = "alias_normalized", nullable = false, length = 160)
    private String aliasNormalized;

    @Column(name = "published_normalized", length = 160)
    private String publishedNormalized;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getConceptId() { return conceptId; }
    public void setConceptId(UUID conceptId) { this.conceptId = conceptId; }
    public String getAliasOriginal() { return aliasOriginal; }
    public void setAliasOriginal(String aliasOriginal) { this.aliasOriginal = aliasOriginal; }
    public String getAliasNormalized() { return aliasNormalized; }
    public void setAliasNormalized(String aliasNormalized) { this.aliasNormalized = aliasNormalized; }
    public String getPublishedNormalized() { return publishedNormalized; }
    public void setPublishedNormalized(String publishedNormalized) { this.publishedNormalized = publishedNormalized; }
}
