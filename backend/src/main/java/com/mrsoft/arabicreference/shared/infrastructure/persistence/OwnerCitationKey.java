package com.mrsoft.arabicreference.shared.infrastructure.persistence;

import java.io.Serializable;
import java.util.UUID;

public class OwnerCitationKey implements Serializable {

    private UUID ownerId;
    private UUID citationId;

    public OwnerCitationKey() {
    }

    public OwnerCitationKey(UUID ownerId, UUID citationId) {
        this.ownerId = ownerId;
        this.citationId = citationId;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof OwnerCitationKey key && ownerId.equals(key.ownerId) && citationId.equals(key.citationId);
    }

    @Override
    public int hashCode() {
        return ownerId.hashCode() * 31 + citationId.hashCode();
    }
}
