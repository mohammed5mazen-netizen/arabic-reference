package com.mrsoft.arabicreference.morphology.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MorphologyAnalysisCitationRepository extends JpaRepository<MorphologyAnalysisCitationEntity, MorphologyCitationKey> {

    List<MorphologyAnalysisCitationEntity> findByOwnerId(UUID ownerId);

    List<MorphologyAnalysisCitationEntity> findByOwnerIdIn(Collection<UUID> ownerIds);

    boolean existsByOwnerIdAndCitationId(UUID ownerId, UUID citationId);
}
