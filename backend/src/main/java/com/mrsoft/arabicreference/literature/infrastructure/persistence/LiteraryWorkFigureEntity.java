package com.mrsoft.arabicreference.literature.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "literary_work_figure")
@IdClass(LiteraryWorkFigureEntity.Key.class)
public class LiteraryWorkFigureEntity {

    @Id
    @Column(name = "work_id")
    private UUID workId;

    @Id
    @Column(name = "figure_id")
    private UUID figureId;

    @Column(name = "role_label", length = 80)
    private String roleLabel;

    public LiteraryWorkFigureEntity() {
    }

    public LiteraryWorkFigureEntity(UUID workId, UUID figureId, String roleLabel) {
        this.workId = workId;
        this.figureId = figureId;
        this.roleLabel = roleLabel;
    }

    public UUID getWorkId() { return workId; }
    public UUID getFigureId() { return figureId; }
    public String getRoleLabel() { return roleLabel; }

    public static class Key implements Serializable {
        private UUID workId;
        private UUID figureId;

        public Key() {
        }

        public Key(UUID workId, UUID figureId) {
            this.workId = workId;
            this.figureId = figureId;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Key key && workId.equals(key.workId) && figureId.equals(key.figureId);
        }

        @Override
        public int hashCode() {
            return workId.hashCode() * 31 + figureId.hashCode();
        }
    }
}
