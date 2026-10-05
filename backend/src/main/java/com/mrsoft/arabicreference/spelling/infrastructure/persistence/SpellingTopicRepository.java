package com.mrsoft.arabicreference.spelling.infrastructure.persistence;

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

public interface SpellingTopicRepository extends JpaRepository<SpellingTopicEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select topic from SpellingTopicEntity topic where topic.id = :id")
    Optional<SpellingTopicEntity> lockById(@Param("id") UUID id);

    Optional<SpellingTopicEntity> findBySlug(String slug);

    Page<SpellingTopicEntity> findAllByOrderByUpdatedAtDesc(Pageable pageable);

    List<SpellingTopicEntity> findByStatusOrderByUpdatedAtDesc(PublicationStatus status);

    @Query("""
            select topic from SpellingTopicEntity topic
            where topic.publishedSnapshot is not null and topic.status <> :archived
            order by topic.displayOrder asc, topic.titleNormalized asc
            """)
    List<SpellingTopicEntity> visibleToPublic(@Param("archived") PublicationStatus archived);
}
