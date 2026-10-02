package com.mrsoft.arabicreference.dictionary.infrastructure.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RootCitationRepository extends JpaRepository<RootCitationEntity, EvidenceLinkKey> {

    boolean existsByOwnerIdAndCitationId(UUID ownerId, UUID citationId);
}
