package com.mrsoft.arabicreference.literature.infrastructure.persistence;

import java.io.Serializable;
import java.util.UUID;

public class LiterarySchoolFigureKey implements Serializable {

    private UUID schoolId;
    private UUID figureId;

    public LiterarySchoolFigureKey() {
    }

    public LiterarySchoolFigureKey(UUID schoolId, UUID figureId) {
        this.schoolId = schoolId;
        this.figureId = figureId;
    }

    public UUID getSchoolId() { return schoolId; }
    public void setSchoolId(UUID schoolId) { this.schoolId = schoolId; }
    public UUID getFigureId() { return figureId; }
    public void setFigureId(UUID figureId) { this.figureId = figureId; }

    @Override
    public boolean equals(Object other) {
        return other instanceof LiterarySchoolFigureKey key && schoolId.equals(key.schoolId) && figureId.equals(key.figureId);
    }

    @Override
    public int hashCode() {
        return schoolId.hashCode() * 31 + figureId.hashCode();
    }
}
