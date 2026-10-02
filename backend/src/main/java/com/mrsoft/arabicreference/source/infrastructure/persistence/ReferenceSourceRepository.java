package com.mrsoft.arabicreference.source.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReferenceSourceRepository extends JpaRepository<ReferenceSourceEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select source from ReferenceSourceEntity source where source.id = :id")
    Optional<ReferenceSourceEntity> lockById(@Param("id") UUID id);

    Page<ReferenceSourceEntity> findAllByOrderByTitleAsc(Pageable pageable);
}
