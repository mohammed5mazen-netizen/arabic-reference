package com.mrsoft.arabicreference.content.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleSectionRepository extends JpaRepository<ArticleSectionEntity, UUID> {

    List<ArticleSectionEntity> findByArticleIdOrderByDisplayOrderAsc(UUID articleId);

    Optional<ArticleSectionEntity> findByIdAndArticleId(UUID id, UUID articleId);
}
