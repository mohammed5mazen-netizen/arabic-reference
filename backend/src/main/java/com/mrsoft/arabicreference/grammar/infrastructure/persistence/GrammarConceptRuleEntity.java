package com.mrsoft.arabicreference.grammar.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "grammar_concept_rule")
@IdClass(GrammarConceptRuleEntity.Key.class)
public class GrammarConceptRuleEntity {

    @Id
    @Column(name = "concept_id")
    private UUID conceptId;

    @Id
    @Column(name = "rule_id")
    private UUID ruleId;

    public GrammarConceptRuleEntity() {
    }

    public GrammarConceptRuleEntity(UUID conceptId, UUID ruleId) {
        this.conceptId = conceptId;
        this.ruleId = ruleId;
    }

    public UUID getConceptId() { return conceptId; }
    public UUID getRuleId() { return ruleId; }

    public static class Key implements Serializable {
        private UUID conceptId;
        private UUID ruleId;

        public Key() {
        }

        public Key(UUID conceptId, UUID ruleId) {
            this.conceptId = conceptId;
            this.ruleId = ruleId;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Key key && conceptId.equals(key.conceptId) && ruleId.equals(key.ruleId);
        }

        @Override
        public int hashCode() {
            return conceptId.hashCode() * 31 + ruleId.hashCode();
        }
    }
}
