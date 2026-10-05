package com.mrsoft.arabicreference.spelling.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpellingExampleRepository extends JpaRepository<SpellingExampleEntity, UUID> {

    List<SpellingExampleEntity> findByRuleIdOrderByDisplayOrderAsc(UUID ruleId);
}
