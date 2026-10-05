package com.mrsoft.arabicreference.dictionary.infrastructure.persistence;

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

public interface LexicalEntryRepository extends JpaRepository<LexicalEntryEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select entry from LexicalEntryEntity entry where entry.id = :id")
    Optional<LexicalEntryEntity> lockById(@Param("id") UUID id);

    Optional<LexicalEntryEntity> findBySlug(String slug);

    @Query("""
            select entry from LexicalEntryEntity entry
            where (:lemma is null or entry.lemmaNormalized = :lemma)
            """)
    Page<LexicalEntryEntity> search(@Param("lemma") String lemma, Pageable pageable);

    @Query("""
            select entry from LexicalEntryEntity entry
            where entry.publishedLemmaNormalized = :lemma
              and entry.publishedSnapshot is not null
              and entry.status <> :archived
            """)
    Page<LexicalEntryEntity> findPublishedByLemma(
            @Param("lemma") String lemma,
            @Param("archived") PublicationStatus archived,
            Pageable pageable);

    Page<LexicalEntryEntity> findByStatus(PublicationStatus status, Pageable pageable);

    @Query("""
            select entry from LexicalEntryEntity entry
            where entry.rootId = :rootId
              and entry.publishedSnapshot is not null
              and entry.status <> :archived
            """)
    List<LexicalEntryEntity> findPublishedByRoot(
            @Param("rootId") UUID rootId,
            @Param("archived") PublicationStatus archived);

    @Query("""
            select entry from LexicalEntryEntity entry
            where entry.publishedSnapshot is not null
              and entry.status <> :archived
            """)
    List<LexicalEntryEntity> visibleToPublic(@Param("archived") PublicationStatus archived);
}
