package com.mrsoft.arabicreference.literature.infrastructure.persistence;

import java.io.Serializable;
import java.util.UUID;

public class LiteraryFigureEraKey implements Serializable {

    private UUID figureId;
    private UUID eraId;

    public LiteraryFigureEraKey() {
    }

    public LiteraryFigureEraKey(UUID figureId, UUID eraId) {
        this.figureId = figureId;
        this.eraId = eraId;
    }

    public UUID getFigureId() { return figureId; }
    public void setFigureId(UUID figureId) { this.figureId = figureId; }
    public UUID getEraId() { return eraId; }
    public void setEraId(UUID eraId) { this.eraId = eraId; }

    @Override
    public boolean equals(Object other) {
        return other instanceof LiteraryFigureEraKey key && figureId.equals(key.figureId) && eraId.equals(key.eraId);
    }

    @Override
    public int hashCode() {
        return figureId.hashCode() * 31 + eraId.hashCode();
    }
}
