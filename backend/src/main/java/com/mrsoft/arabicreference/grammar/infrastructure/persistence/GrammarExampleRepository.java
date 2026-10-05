package com.mrsoft.arabicreference.grammar.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GrammarExampleRepository extends JpaRepository<GrammarExampleEntity, UUID> {
    List<GrammarExampleEntity> findByRuleIdOrderByDisplayOrderAsc(UUID ruleId);
    Page<GrammarExampleEntity> findAllByOrderByDisplayOrderAsc(Pageable pageable);
}
