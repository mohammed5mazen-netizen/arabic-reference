package com.mrsoft.arabicreference.literature.infrastructure.persistence;

import com.mrsoft.arabicreference.shared.infrastructure.persistence.OwnerCitationKey;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LiteraryEraCitationRepository extends JpaRepository<LiteraryEraCitationEntity, OwnerCitationKey> {

    boolean existsByOwnerIdAndCitationId(UUID ownerId, UUID citationId);

    List<LiteraryEraCitationEntity> findByOwnerId(UUID ownerId);
}
