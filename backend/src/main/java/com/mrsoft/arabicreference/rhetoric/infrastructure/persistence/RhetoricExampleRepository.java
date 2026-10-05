package com.mrsoft.arabicreference.rhetoric.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RhetoricExampleRepository extends JpaRepository<RhetoricExampleEntity, UUID> {
    List<RhetoricExampleEntity> findByDeviceIdOrderByDisplayOrderAsc(UUID deviceId);
}
