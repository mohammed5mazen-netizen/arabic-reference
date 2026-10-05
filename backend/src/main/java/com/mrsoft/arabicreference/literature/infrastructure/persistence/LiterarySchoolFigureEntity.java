package com.mrsoft.arabicreference.literature.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "literary_school_figure")
@IdClass(LiterarySchoolFigureEntity.Key.class)
public class LiterarySchoolFigureEntity {

    @Id
    @Column(name = "school_id")
    private UUID schoolId;

    @Id
    @Column(name = "figure_id")
    private UUID figureId;

    public LiterarySchoolFigureEntity() {
    }

    public LiterarySchoolFigureEntity(UUID schoolId, UUID figureId) {
        this.schoolId = schoolId;
        this.figureId = figureId;
    }

    public UUID getSchoolId() { return schoolId; }
    public UUID getFigureId() { return figureId; }

    public static class Key implements Serializable {
        private UUID schoolId;
        private UUID figureId;

        public Key() {
        }

        public Key(UUID schoolId, UUID figureId) {
            this.schoolId = schoolId;
            this.figureId = figureId;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Key key && schoolId.equals(key.schoolId) && figureId.equals(key.figureId);
        }

        @Override
        public int hashCode() {
            return schoolId.hashCode() * 31 + figureId.hashCode();
        }
    }
}
