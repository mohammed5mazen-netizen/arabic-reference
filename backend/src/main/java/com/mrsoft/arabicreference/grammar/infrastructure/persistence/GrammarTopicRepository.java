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

public interface GrammarTopicRepository extends JpaRepository<GrammarTopicEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select topic from GrammarTopicEntity topic where topic.id = :id")
    Optional<GrammarTopicEntity> lockById(@Param("id") UUID id);

    Optional<GrammarTopicEntity> findBySlug(String slug);

    Page<GrammarTopicEntity> findAllByOrderByUpdatedAtDesc(Pageable pageable);

    @Query("""
            select topic from GrammarTopicEntity topic
            where topic.publishedSnapshot is not null
              and topic.status <> :archived
            order by topic.publishedDisplayOrder asc, topic.publishedTitle asc
            """)
    Page<GrammarTopicEntity> published(@Param("archived") PublicationStatus archived, Pageable pageable);

    @Query("""
            select topic from GrammarTopicEntity topic
            where topic.publishedParentId = :parentId
              and topic.publishedSnapshot is not null
              and topic.status <> :archived
            order by topic.publishedDisplayOrder asc, topic.publishedTitle asc
            """)
    List<GrammarTopicEntity> publishedChildren(@Param("parentId") UUID parentId, @Param("archived") PublicationStatus archived);

    @Query("""
            select topic from GrammarTopicEntity topic
            where topic.publishedSnapshot is not null
              and topic.status <> :archived
            """)
    List<GrammarTopicEntity> visibleToPublic(@Param("archived") PublicationStatus archived);
}
