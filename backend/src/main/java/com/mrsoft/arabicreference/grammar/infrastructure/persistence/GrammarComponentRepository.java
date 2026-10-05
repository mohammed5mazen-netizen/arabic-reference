package com.mrsoft.arabicreference.grammar.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GrammarComponentRepository extends JpaRepository<GrammarRuleComponentEntity, UUID> {
    List<GrammarRuleComponentEntity> findByRuleIdOrderByDisplayOrderAsc(UUID ruleId);
}
