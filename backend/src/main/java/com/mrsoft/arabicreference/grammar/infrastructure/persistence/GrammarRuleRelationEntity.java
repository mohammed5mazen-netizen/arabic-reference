package com.mrsoft.arabicreference.grammar.infrastructure.persistence;

import com.mrsoft.arabicreference.grammar.domain.RuleRelationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "grammar_rule_relation")
public class GrammarRuleRelationEntity {

    @Id
    private UUID id;

    @Column(name = "source_rule_id", nullable = false)
    private UUID sourceRuleId;

    @Column(name = "target_rule_id", nullable = false)
    private UUID targetRuleId;

    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type", nullable = false, length = 32)
    private RuleRelationType relationType;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getSourceRuleId() { return sourceRuleId; }
    public void setSourceRuleId(UUID sourceRuleId) { this.sourceRuleId = sourceRuleId; }
    public UUID getTargetRuleId() { return targetRuleId; }
    public void setTargetRuleId(UUID targetRuleId) { this.targetRuleId = targetRuleId; }
    public RuleRelationType getRelationType() { return relationType; }
    public void setRelationType(RuleRelationType relationType) { this.relationType = relationType; }
}
