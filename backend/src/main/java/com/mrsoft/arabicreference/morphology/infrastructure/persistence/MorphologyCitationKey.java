package com.mrsoft.arabicreference.morphology.infrastructure.persistence;

import java.io.Serializable;
import java.util.UUID;

public class MorphologyCitationKey implements Serializable {

    private UUID ownerId;
    private UUID citationId;

    public MorphologyCitationKey() {
    }

    public MorphologyCitationKey(UUID ownerId, UUID citationId) {
        this.ownerId = ownerId;
        this.citationId = citationId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MorphologyCitationKey key)) {
            return false;
        }
        return ownerId.equals(key.ownerId) && citationId.equals(key.citationId);
    }

    @Override
    public int hashCode() {
        return ownerId.hashCode() * 31 + citationId.hashCode();
    }
}
