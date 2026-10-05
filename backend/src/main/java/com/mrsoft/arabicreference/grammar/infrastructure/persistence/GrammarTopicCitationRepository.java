package com.mrsoft.arabicreference.grammar.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GrammarTopicCitationRepository extends JpaRepository<GrammarTopicCitationEntity, GrammarCitationKey> {
    List<GrammarTopicCitationEntity> findByOwnerId(UUID ownerId);
    boolean existsByOwnerIdAndCitationId(UUID ownerId, UUID citationId);
}
