package com.mrsoft.arabicreference.grammar.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "grammar_dependency")
public class GrammarDependencyEntity {

    @Id
    private UUID id;

    @Column(name = "annotation_id", nullable = false)
    private UUID annotationId;

    @Column(name = "governor_position", nullable = false)
    private int governorPosition;

    @Column(name = "dependent_position", nullable = false)
    private int dependentPosition;

    @Column(name = "relation_label", nullable = false, length = 80)
    private String relationLabel;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getAnnotationId() { return annotationId; }
    public void setAnnotationId(UUID annotationId) { this.annotationId = annotationId; }
    public int getGovernorPosition() { return governorPosition; }
    public void setGovernorPosition(int governorPosition) { this.governorPosition = governorPosition; }
    public int getDependentPosition() { return dependentPosition; }
    public void setDependentPosition(int dependentPosition) { this.dependentPosition = dependentPosition; }
    public String getRelationLabel() { return relationLabel; }
    public void setRelationLabel(String relationLabel) { this.relationLabel = relationLabel; }
}
