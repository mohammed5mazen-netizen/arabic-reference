package com.mrsoft.arabicreference.grammar.infrastructure.persistence;

import java.io.Serializable;
import java.util.UUID;

public class GrammarCitationKey implements Serializable {

    private UUID ownerId;
    private UUID citationId;

    public GrammarCitationKey() {
    }

    public GrammarCitationKey(UUID ownerId, UUID citationId) {
        this.ownerId = ownerId;
        this.citationId = citationId;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof GrammarCitationKey key && ownerId.equals(key.ownerId) && citationId.equals(key.citationId);
    }

    @Override
    public int hashCode() {
        return ownerId.hashCode() * 31 + citationId.hashCode();
    }
}
