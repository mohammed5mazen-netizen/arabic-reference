package com.mrsoft.arabicreference.grammar.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GrammarConceptRuleRepository extends JpaRepository<GrammarConceptRuleEntity, GrammarConceptRuleEntity.Key> {
    List<GrammarConceptRuleEntity> findByConceptId(UUID conceptId);
    List<GrammarConceptRuleEntity> findByRuleId(UUID ruleId);
    boolean existsByConceptIdAndRuleId(UUID conceptId, UUID ruleId);
}
