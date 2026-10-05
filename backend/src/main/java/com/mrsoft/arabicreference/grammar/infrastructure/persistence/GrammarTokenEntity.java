package com.mrsoft.arabicreference.grammar.infrastructure.persistence;

import com.mrsoft.arabicreference.grammar.domain.GrammaticalState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "grammar_token")
public class GrammarTokenEntity {

    @Id
    private UUID id;

    @Column(name = "annotation_id", nullable = false)
    private UUID annotationId;

    @Column(nullable = false, length = 80)
    private String surface;

    @Column(nullable = false, length = 80)
    private String normalized;

    @Column(nullable = false)
    private int position;

    @Column(name = "lexical_entry_id")
    private UUID lexicalEntryId;

    @Column(name = "morphology_analysis_id")
    private UUID morphologyAnalysisId;

    @Column(name = "role_code", length = 40)
    private String roleCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "grammatical_state", length = 16)
    private GrammaticalState grammaticalState;

    @Column(length = 500)
    private String explanation;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getAnnotationId() { return annotationId; }
    public void setAnnotationId(UUID annotationId) { this.annotationId = annotationId; }
    public String getSurface() { return surface; }
    public void setSurface(String surface) { this.surface = surface; }
    public String getNormalized() { return normalized; }
    public void setNormalized(String normalized) { this.normalized = normalized; }
    public int getPosition() { return position; }
    public void setPosition(int position) { this.position = position; }
    public UUID getLexicalEntryId() { return lexicalEntryId; }
    public void setLexicalEntryId(UUID lexicalEntryId) { this.lexicalEntryId = lexicalEntryId; }
    public UUID getMorphologyAnalysisId() { return morphologyAnalysisId; }
    public void setMorphologyAnalysisId(UUID morphologyAnalysisId) { this.morphologyAnalysisId = morphologyAnalysisId; }
    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }
    public GrammaticalState getGrammaticalState() { return grammaticalState; }
    public void setGrammaticalState(GrammaticalState grammaticalState) { this.grammaticalState = grammaticalState; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
}
