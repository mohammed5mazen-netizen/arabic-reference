package com.mrsoft.arabicreference.dictionary.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SenseCitationRepository extends JpaRepository<SenseCitationEntity, EvidenceLinkKey> {

    List<SenseCitationEntity> findByOwnerId(UUID ownerId);

    boolean existsByOwnerIdAndCitationId(UUID ownerId, UUID citationId);
}
