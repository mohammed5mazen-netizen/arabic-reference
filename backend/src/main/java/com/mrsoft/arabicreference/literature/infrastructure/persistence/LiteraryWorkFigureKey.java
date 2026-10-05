package com.mrsoft.arabicreference.literature.infrastructure.persistence;

import java.io.Serializable;
import java.util.UUID;

public class LiteraryWorkFigureKey implements Serializable {

    private UUID workId;
    private UUID figureId;

    public LiteraryWorkFigureKey() {
    }

    public LiteraryWorkFigureKey(UUID workId, UUID figureId) {
        this.workId = workId;
        this.figureId = figureId;
    }

    public UUID getWorkId() { return workId; }
    public void setWorkId(UUID workId) { this.workId = workId; }
    public UUID getFigureId() { return figureId; }
    public void setFigureId(UUID figureId) { this.figureId = figureId; }

    @Override
    public boolean equals(Object other) {
        return other instanceof LiteraryWorkFigureKey key && workId.equals(key.workId) && figureId.equals(key.figureId);
    }

    @Override
    public int hashCode() {
        return workId.hashCode() * 31 + figureId.hashCode();
    }
}
