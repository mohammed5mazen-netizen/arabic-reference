package com.mrsoft.arabicreference.literature.infrastructure.persistence;

import com.mrsoft.arabicreference.literature.domain.LiteraryRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "literary_figure_role")
@IdClass(LiteraryFigureRoleEntity.Key.class)
public class LiteraryFigureRoleEntity {

    @Id
    @Column(name = "figure_id")
    private UUID figureId;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private LiteraryRole role;

    public LiteraryFigureRoleEntity() {
    }

    public LiteraryFigureRoleEntity(UUID figureId, LiteraryRole role) {
        this.figureId = figureId;
        this.role = role;
    }

    public UUID getFigureId() { return figureId; }
    public LiteraryRole getRole() { return role; }

    public static class Key implements Serializable {
        private UUID figureId;
        private LiteraryRole role;

        public Key() {
        }

        public Key(UUID figureId, LiteraryRole role) {
            this.figureId = figureId;
            this.role = role;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Key key && figureId.equals(key.figureId) && role == key.role;
        }

        @Override
        public int hashCode() {
            return figureId.hashCode() * 31 + role.hashCode();
        }
    }
}
