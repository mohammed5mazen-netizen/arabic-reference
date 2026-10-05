package com.mrsoft.arabicreference.grammar.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GrammarConceptCitationRepository extends JpaRepository<GrammarConceptCitationEntity, GrammarCitationKey> {
    List<GrammarConceptCitationEntity> findByOwnerId(UUID ownerId);
    boolean existsByOwnerIdAndCitationId(UUID ownerId, UUID citationId);
}
