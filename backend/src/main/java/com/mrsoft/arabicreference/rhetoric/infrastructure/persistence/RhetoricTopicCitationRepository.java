package com.mrsoft.arabicreference.rhetoric.infrastructure.persistence;

import com.mrsoft.arabicreference.shared.infrastructure.persistence.OwnerCitationKey;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RhetoricTopicCitationRepository extends JpaRepository<RhetoricTopicCitationEntity, OwnerCitationKey> {
    boolean existsByOwnerIdAndCitationId(UUID ownerId, UUID citationId);
    List<RhetoricTopicCitationEntity> findByOwnerId(UUID ownerId);
}
