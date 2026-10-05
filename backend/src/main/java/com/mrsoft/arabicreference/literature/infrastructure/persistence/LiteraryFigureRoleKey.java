package com.mrsoft.arabicreference.literature.infrastructure.persistence;

import com.mrsoft.arabicreference.literature.domain.LiteraryRole;
import java.io.Serializable;
import java.util.UUID;

public class LiteraryFigureRoleKey implements Serializable {

    private UUID figureId;
    private LiteraryRole role;

    public LiteraryFigureRoleKey() {
    }

    public LiteraryFigureRoleKey(UUID figureId, LiteraryRole role) {
        this.figureId = figureId;
        this.role = role;
    }

    public UUID getFigureId() { return figureId; }
    public void setFigureId(UUID figureId) { this.figureId = figureId; }
    public LiteraryRole getRole() { return role; }
    public void setRole(LiteraryRole role) { this.role = role; }

    @Override
    public boolean equals(Object other) {
        return other instanceof LiteraryFigureRoleKey key && figureId.equals(key.figureId) && role == key.role;
    }

    @Override
    public int hashCode() {
        return figureId.hashCode() * 31 + role.hashCode();
    }
}
