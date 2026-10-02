package com.mrsoft.arabicreference.morphology.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "morphology_analysis_citation")
@IdClass(MorphologyCitationKey.class)
public class MorphologyAnalysisCitationEntity {

    @Id
    @Column(name = "analysis_id")
    private UUID ownerId;

    @Id
    @Column(name = "citation_id")
    private UUID citationId;

    public MorphologyAnalysisCitationEntity() {
    }

    public MorphologyAnalysisCitationEntity(UUID ownerId, UUID citationId) {
        this.ownerId = ownerId;
        this.citationId = citationId;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public UUID getCitationId() {
        return citationId;
    }
}
