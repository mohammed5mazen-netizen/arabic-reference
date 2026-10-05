package com.mrsoft.arabicreference.rhetoric.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RhetoricComponentRepository extends JpaRepository<RhetoricComponentEntity, UUID> {
    List<RhetoricComponentEntity> findByDeviceIdOrderByDisplayOrderAsc(UUID deviceId);
}
