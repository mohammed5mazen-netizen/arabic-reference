package com.mrsoft.arabicreference.rhetoric.infrastructure.persistence;

import com.mrsoft.arabicreference.rhetoric.domain.RhetoricRelationKind;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RhetoricRelationRepository extends JpaRepository<RhetoricRelationEntity, UUID> {
    List<RhetoricRelationEntity> findBySourceDeviceId(UUID sourceDeviceId);
    boolean existsBySourceDeviceIdAndTargetDeviceIdAndKind(UUID sourceDeviceId, UUID targetDeviceId, RhetoricRelationKind kind);
}
