package com.mrsoft.arabicreference.dictionary.infrastructure.persistence;

import java.io.Serializable;
import java.util.UUID;

public class EvidenceLinkKey implements Serializable {

    private UUID ownerId;
    private UUID citationId;

    public EvidenceLinkKey() {
    }

    public EvidenceLinkKey(UUID ownerId, UUID citationId) {
        this.ownerId = ownerId;
        this.citationId = citationId;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof EvidenceLinkKey key && ownerId.equals(key.ownerId) && citationId.equals(key.citationId);
    }

    @Override
    public int hashCode() {
        return ownerId.hashCode() * 31 + citationId.hashCode();
    }
}
