package com.mrsoft.arabicreference.dictionary.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "sense_citation")
@IdClass(EvidenceLinkKey.class)
public class SenseCitationEntity {

    @Id
    @Column(name = "sense_id")
    private UUID ownerId;

    @Id
    @Column(name = "citation_id")
    private UUID citationId;

    public SenseCitationEntity() {
    }

    public SenseCitationEntity(UUID ownerId, UUID citationId) {
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
