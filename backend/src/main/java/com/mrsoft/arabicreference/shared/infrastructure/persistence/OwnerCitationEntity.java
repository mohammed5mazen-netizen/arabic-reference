package com.mrsoft.arabicreference.shared.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.MappedSuperclass;
import java.util.UUID;

@MappedSuperclass
@IdClass(OwnerCitationKey.class)
public abstract class OwnerCitationEntity {

    @Id
    @Column(name = "owner_id")
    private UUID ownerId;

    @Id
    @Column(name = "citation_id")
    private UUID citationId;

    protected OwnerCitationEntity() {
    }

    protected OwnerCitationEntity(UUID ownerId, UUID citationId) {
        this.ownerId = ownerId;
        this.citationId = citationId;
    }

    public UUID getOwnerId() { return ownerId; }
    public UUID getCitationId() { return citationId; }
}
