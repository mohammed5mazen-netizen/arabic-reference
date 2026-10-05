package com.mrsoft.arabicreference.literature.infrastructure.persistence;

import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LiteraryEraRepository extends JpaRepository<LiteraryEraEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select era from LiteraryEraEntity era where era.id = :id")
    Optional<LiteraryEraEntity> lockById(@Param("id") UUID id);

    Optional<LiteraryEraEntity> findBySlug(String slug);

    Page<LiteraryEraEntity> findAllByOrderByUpdatedAtDesc(Pageable pageable);

    List<LiteraryEraEntity> findByStatusOrderByUpdatedAtDesc(PublicationStatus status);

    @Query("""
            select era from LiteraryEraEntity era
            where era.publishedSnapshot is not null and era.status <> :archived
            order by era.displayOrder asc, era.nameNormalized asc
            """)
    List<LiteraryEraEntity> visibleToPublic(@Param("archived") PublicationStatus archived);
}
