package com.mrsoft.arabicreference.grammar.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "grammar_rule_citation")
@IdClass(GrammarCitationKey.class)
public class GrammarRuleCitationEntity {

    @Id
    @Column(name = "rule_id")
    private UUID ownerId;

    @Id
    @Column(name = "citation_id")
    private UUID citationId;

    public GrammarRuleCitationEntity() {
    }

    public GrammarRuleCitationEntity(UUID ownerId, UUID citationId) {
        this.ownerId = ownerId;
        this.citationId = citationId;
    }

    public UUID getOwnerId() { return ownerId; }
    public UUID getCitationId() { return citationId; }
}
