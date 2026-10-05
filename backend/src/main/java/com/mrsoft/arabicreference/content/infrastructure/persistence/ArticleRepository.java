package com.mrsoft.arabicreference.content.infrastructure.persistence;

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

public interface ArticleRepository extends JpaRepository<ArticleEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select article from ArticleEntity article where article.id = :id")
    Optional<ArticleEntity> lockById(@Param("id") UUID id);

    Optional<ArticleEntity> findBySlug(String slug);

    Page<ArticleEntity> findAllByOrderByUpdatedAtDesc(Pageable pageable);

    List<ArticleEntity> findByStatusOrderByUpdatedAtDesc(PublicationStatus status);

    @Query("""
            select article from ArticleEntity article
            where article.publishedSnapshot is not null and article.status <> :archived
            order by article.updatedAt desc
            """)
    List<ArticleEntity> visibleToPublic(@Param("archived") PublicationStatus archived);
}
