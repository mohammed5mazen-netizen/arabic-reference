package com.mrsoft.arabicreference.literature.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "literary_figure_era")
@IdClass(LiteraryFigureEraEntity.Key.class)
public class LiteraryFigureEraEntity {

    @Id
    @Column(name = "figure_id")
    private UUID figureId;

    @Id
    @Column(name = "era_id")
    private UUID eraId;

    public LiteraryFigureEraEntity() {
    }

    public LiteraryFigureEraEntity(UUID figureId, UUID eraId) {
        this.figureId = figureId;
        this.eraId = eraId;
    }

    public UUID getFigureId() { return figureId; }
    public UUID getEraId() { return eraId; }

    public static class Key implements Serializable {
        private UUID figureId;
        private UUID eraId;

        public Key() {
        }

        public Key(UUID figureId, UUID eraId) {
            this.figureId = figureId;
            this.eraId = eraId;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Key key && figureId.equals(key.figureId) && eraId.equals(key.eraId);
        }

        @Override
        public int hashCode() {
            return figureId.hashCode() * 31 + eraId.hashCode();
        }
    }
}
