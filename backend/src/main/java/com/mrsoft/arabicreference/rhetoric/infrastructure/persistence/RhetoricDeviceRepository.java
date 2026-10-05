package com.mrsoft.arabicreference.rhetoric.infrastructure.persistence;

import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RhetoricDeviceRepository extends JpaRepository<RhetoricDeviceEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select device from RhetoricDeviceEntity device where device.id = :id")
    Optional<RhetoricDeviceEntity> lockById(@Param("id") UUID id);
    Optional<RhetoricDeviceEntity> findBySlug(String slug);
    List<RhetoricDeviceEntity> findByStatusOrderByUpdatedAtDesc(PublicationStatus status);
    @Query("select device from RhetoricDeviceEntity device where device.publishedSnapshot is not null and device.status <> :archived")
    List<RhetoricDeviceEntity> visibleToPublic(@Param("archived") PublicationStatus archived);
}
