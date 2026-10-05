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

public interface GrammarRuleRepository extends JpaRepository<GrammarRuleEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select rule from GrammarRuleEntity rule where rule.id = :id")
    Optional<GrammarRuleEntity> lockById(@Param("id") UUID id);

    Optional<GrammarRuleEntity> findBySlug(String slug);

    Page<GrammarRuleEntity> findAllByOrderByUpdatedAtDesc(Pageable pageable);

    List<GrammarRuleEntity> findByTopicIdOrderByDisplayOrderAsc(UUID topicId);

    @Query("""
            select rule from GrammarRuleEntity rule
            where rule.publishedTopicId = :topicId
              and rule.publishedSnapshot is not null
              and rule.status <> :archived
            order by rule.publishedDisplayOrder asc, rule.publishedTitle asc
            """)
    List<GrammarRuleEntity> publishedForTopic(@Param("topicId") UUID topicId, @Param("archived") PublicationStatus archived);
}
