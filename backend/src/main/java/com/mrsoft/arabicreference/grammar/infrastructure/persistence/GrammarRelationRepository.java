package com.mrsoft.arabicreference.grammar.infrastructure.persistence;

import com.mrsoft.arabicreference.grammar.domain.RuleRelationType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GrammarRelationRepository extends JpaRepository<GrammarRuleRelationEntity, UUID> {
    List<GrammarRuleRelationEntity> findBySourceRuleId(UUID sourceRuleId);
    boolean existsBySourceRuleIdAndTargetRuleIdAndRelationType(UUID sourceRuleId, UUID targetRuleId, RuleRelationType relationType);
}
