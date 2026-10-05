package com.mrsoft.arabicreference.grammar.infrastructure.persistence;

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

public interface GrammarConceptRepository extends JpaRepository<GrammarConceptEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select concept from GrammarConceptEntity concept where concept.id = :id")
    Optional<GrammarConceptEntity> lockById(@Param("id") UUID id);

    Optional<GrammarConceptEntity> findBySlug(String slug);

    Page<GrammarConceptEntity> findAllByOrderByUpdatedAtDesc(Pageable pageable);

    @Query("""
            select concept from GrammarConceptEntity concept
            where concept.publishedSnapshot is not null
              and concept.status <> :archived
            """)
    List<GrammarConceptEntity> visibleToPublic(@Param("archived") PublicationStatus archived);
}
