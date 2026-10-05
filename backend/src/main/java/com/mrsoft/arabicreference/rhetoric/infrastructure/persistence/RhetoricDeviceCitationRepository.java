package com.mrsoft.arabicreference.rhetoric.infrastructure.persistence;

import com.mrsoft.arabicreference.shared.infrastructure.persistence.OwnerCitationKey;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RhetoricDeviceCitationRepository extends JpaRepository<RhetoricDeviceCitationEntity, OwnerCitationKey> {
    boolean existsByOwnerIdAndCitationId(UUID ownerId, UUID citationId);
    List<RhetoricDeviceCitationEntity> findByOwnerId(UUID ownerId);
}
