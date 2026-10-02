package com.mrsoft.arabicreference.dictionary.infrastructure.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RelationCitationRepository extends JpaRepository<RelationCitationEntity, EvidenceLinkKey> {

    boolean existsByOwnerIdAndCitationId(UUID ownerId, UUID citationId);
}
