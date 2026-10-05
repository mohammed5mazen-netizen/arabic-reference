package com.mrsoft.arabicreference.spelling.infrastructure.persistence;

import com.mrsoft.arabicreference.linguistics.domain.editorial.PublicationStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpellingRuleRepository extends JpaRepository<SpellingRuleEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select rule from SpellingRuleEntity rule where rule.id = :id")
    Optional<SpellingRuleEntity> lockById(@Param("id") UUID id);

    Optional<SpellingRuleEntity> findBySlug(String slug);

    List<SpellingRuleEntity> findByTopicId(UUID topicId);

    List<SpellingRuleEntity> findByStatusOrderByUpdatedAtDesc(PublicationStatus status);

    @Query("""
            select rule from SpellingRuleEntity rule
            where rule.publishedSnapshot is not null and rule.status <> :archived
            """)
    List<SpellingRuleEntity> visibleToPublic(@Param("archived") PublicationStatus archived);
}
