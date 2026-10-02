package com.mrsoft.arabicreference.dictionary.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "relation_citation")
@IdClass(EvidenceLinkKey.class)
public class RelationCitationEntity {

    @Id
    @Column(name = "relation_id")
    private UUID ownerId;

    @Id
    @Column(name = "citation_id")
    private UUID citationId;

    public RelationCitationEntity() {
    }

    public RelationCitationEntity(UUID ownerId, UUID citationId) {
        this.ownerId = ownerId;
        this.citationId = citationId;
    }
}
