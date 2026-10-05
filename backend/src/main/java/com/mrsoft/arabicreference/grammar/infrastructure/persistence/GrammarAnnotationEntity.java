package com.mrsoft.arabicreference.grammar.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "grammar_annotation")
public class GrammarAnnotationEntity extends EditorialRecord {

    @Column(name = "sentence_original", nullable = false, length = 500)
    private String sentenceOriginal;

    @Column(name = "sentence_normalized", nullable = false, length = 500)
    private String sentenceNormalized;

    @Column(name = "citation_id")
    private UUID citationId;

    public String getSentenceOriginal() { return sentenceOriginal; }
    public void setSentenceOriginal(String sentenceOriginal) { this.sentenceOriginal = sentenceOriginal; }
    public String getSentenceNormalized() { return sentenceNormalized; }
    public void setSentenceNormalized(String sentenceNormalized) { this.sentenceNormalized = sentenceNormalized; }
    public UUID getCitationId() { return citationId; }
    public void setCitationId(UUID citationId) { this.citationId = citationId; }
}
