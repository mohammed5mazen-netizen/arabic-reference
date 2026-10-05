package com.mrsoft.arabicreference.morphology.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MorphologyAnalysisRepository extends JpaRepository<MorphologyAnalysisEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select analysis from MorphologyAnalysisEntity analysis where analysis.id = :id")
    Optional<MorphologyAnalysisEntity> lockById(@Param("id") UUID id);

    Page<MorphologyAnalysisEntity> findAllByOrderByUpdatedAtDesc(Pageable pageable);

    List<MorphologyAnalysisEntity> findByLexicalEntryId(UUID lexicalEntryId);

    @Query("""
            select analysis from MorphologyAnalysisEntity analysis
            where analysis.lexicalEntryId in :entryIds
              and analysis.publishedSnapshot is not null
            """)
    List<MorphologyAnalysisEntity> findPublishedByEntries(@Param("entryIds") Collection<UUID> entryIds);

    @Query("""
            select analysis from MorphologyAnalysisEntity analysis
            where analysis.patternId = :patternId
              and analysis.publishedSnapshot is not null
              and analysis.status <> com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus.ARCHIVED
            """)
    List<MorphologyAnalysisEntity> findPublishedByPattern(@Param("patternId") UUID patternId);
}
