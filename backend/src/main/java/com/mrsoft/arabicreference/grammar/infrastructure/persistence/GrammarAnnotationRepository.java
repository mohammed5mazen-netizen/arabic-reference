package com.mrsoft.arabicreference.grammar.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GrammarAnnotationRepository extends JpaRepository<GrammarAnnotationEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select annotation from GrammarAnnotationEntity annotation where annotation.id = :id")
    Optional<GrammarAnnotationEntity> lockById(@Param("id") UUID id);

    Page<GrammarAnnotationEntity> findAllByOrderByUpdatedAtDesc(Pageable pageable);
}
