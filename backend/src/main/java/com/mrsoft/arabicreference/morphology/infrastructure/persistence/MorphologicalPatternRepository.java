package com.mrsoft.arabicreference.morphology.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MorphologicalPatternRepository extends JpaRepository<MorphologicalPatternEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select pattern from MorphologicalPatternEntity pattern where pattern.id = :id")
    Optional<MorphologicalPatternEntity> lockById(@Param("id") UUID id);

    Optional<MorphologicalPatternEntity> findByCode(String code);

    Page<MorphologicalPatternEntity> findAllByOrderByCodeAsc(Pageable pageable);
}
