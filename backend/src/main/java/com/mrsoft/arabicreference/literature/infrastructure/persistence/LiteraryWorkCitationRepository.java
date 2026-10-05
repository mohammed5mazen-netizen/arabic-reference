package com.mrsoft.arabicreference.literature.infrastructure.persistence;

import com.mrsoft.arabicreference.shared.infrastructure.persistence.OwnerCitationKey;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LiteraryWorkCitationRepository extends JpaRepository<LiteraryWorkCitationEntity, OwnerCitationKey> {

    boolean existsByOwnerIdAndCitationId(UUID ownerId, UUID citationId);

    List<LiteraryWorkCitationEntity> findByOwnerId(UUID ownerId);
}
