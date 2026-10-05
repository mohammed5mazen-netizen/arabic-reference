package com.mrsoft.arabicreference.grammar.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GrammarComponentCitationRepository extends JpaRepository<GrammarComponentCitationEntity, GrammarCitationKey> {
    List<GrammarComponentCitationEntity> findByOwnerIdIn(Collection<UUID> ownerIds);
    boolean existsByOwnerIdAndCitationId(UUID ownerId, UUID citationId);
}
