package com.mrsoft.arabicreference.dictionary.infrastructure.persistence;

import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LinguisticRootRepository extends JpaRepository<LinguisticRootEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select root from LinguisticRootEntity root where root.id = :id")
    Optional<LinguisticRootEntity> lockById(@Param("id") UUID id);

    Optional<LinguisticRootEntity> findBySlug(String slug);

    Optional<LinguisticRootEntity> findByRootNormalized(String rootNormalized);

    Page<LinguisticRootEntity> findAllByOrderByRootNormalizedAsc(Pageable pageable);

    @Query("""
            select root from LinguisticRootEntity root
            where (root.slug = :key or root.publishedNormalized = :key)
              and root.publishedSnapshot is not null
              and root.status <> :archived
            """)
    Optional<LinguisticRootEntity> findPublished(@Param("key") String key, @Param("archived") PublicationStatus archived);
}
