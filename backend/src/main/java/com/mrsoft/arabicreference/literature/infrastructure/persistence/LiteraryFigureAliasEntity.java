package com.mrsoft.arabicreference.literature.infrastructure.persistence;

import com.mrsoft.arabicreference.literature.domain.FigureAliasKind;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "literary_figure_alias")
public class LiteraryFigureAliasEntity {

    @Id
    private UUID id;

    @Column(name = "figure_id", nullable = false)
    private UUID figureId;

    @Column(nullable = false, length = 160)
    private String alias;

    @Column(nullable = false, length = 160)
    private String normalized;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private FigureAliasKind kind;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getFigureId() { return figureId; }
    public void setFigureId(UUID figureId) { this.figureId = figureId; }
    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }
    public String getNormalized() { return normalized; }
    public void setNormalized(String normalized) { this.normalized = normalized; }
    public FigureAliasKind getKind() { return kind; }
    public void setKind(FigureAliasKind kind) { this.kind = kind; }
}
