package com.mrsoft.arabicreference.rhetoric.infrastructure.persistence;

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

public interface RhetoricTopicRepository extends JpaRepository<RhetoricTopicEntity, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select topic from RhetoricTopicEntity topic where topic.id = :id")
    Optional<RhetoricTopicEntity> lockById(@Param("id") UUID id);
    Optional<RhetoricTopicEntity> findBySlug(String slug);
    Page<RhetoricTopicEntity> findAllByOrderByUpdatedAtDesc(Pageable pageable);
    List<RhetoricTopicEntity> findByStatusOrderByUpdatedAtDesc(PublicationStatus status);
    @Query("select topic from RhetoricTopicEntity topic where topic.publishedSnapshot is not null and topic.status <> :archived order by topic.displayOrder asc")
    List<RhetoricTopicEntity> visibleToPublic(@Param("archived") PublicationStatus archived);
}
