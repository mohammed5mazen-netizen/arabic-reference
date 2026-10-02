package com.mrsoft.arabicreference.dictionary.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SenseCitationRepository extends JpaRepository<SenseCitationEntity, EvidenceLinkKey> {

    List<SenseCitationEntity> findByOwnerId(UUID ownerId);

    List<SenseCitationEntity> findByOwnerIdIn(Collection<UUID> ownerIds);

    boolean existsByOwnerIdAndCitationId(UUID ownerId, UUID citationId);
}
