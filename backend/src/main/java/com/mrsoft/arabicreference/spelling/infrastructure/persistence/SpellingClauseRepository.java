package com.mrsoft.arabicreference.spelling.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpellingClauseRepository extends JpaRepository<SpellingClauseEntity, UUID> {

    List<SpellingClauseEntity> findByRuleIdOrderByDisplayOrderAsc(UUID ruleId);
}
