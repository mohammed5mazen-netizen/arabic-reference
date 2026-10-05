package com.mrsoft.arabicreference.grammar.infrastructure.persistence;

import com.mrsoft.arabicreference.grammar.domain.StateKind;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "grammar_role")
public class GrammarRoleEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 40)
    private String code;

    @Column(name = "label_ar", nullable = false, length = 80)
    private String labelAr;

    @Enumerated(EnumType.STRING)
    @Column(name = "state_kind", nullable = false, length = 20)
    private StateKind stateKind;

    @Column(nullable = false)
    private boolean active;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getLabelAr() { return labelAr; }
    public void setLabelAr(String labelAr) { this.labelAr = labelAr; }
    public StateKind getStateKind() { return stateKind; }
    public void setStateKind(StateKind stateKind) { this.stateKind = stateKind; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
