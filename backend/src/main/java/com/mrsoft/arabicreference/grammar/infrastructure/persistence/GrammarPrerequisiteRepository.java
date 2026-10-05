package com.mrsoft.arabicreference.grammar.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GrammarPrerequisiteRepository extends JpaRepository<GrammarPrerequisiteEntity, GrammarPrerequisiteKey> {
    List<GrammarPrerequisiteEntity> findByTopicId(UUID topicId);
    boolean existsByTopicIdAndRequiredTopicId(UUID topicId, UUID requiredTopicId);
}
