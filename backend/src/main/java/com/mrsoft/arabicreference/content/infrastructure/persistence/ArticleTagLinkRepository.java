package com.mrsoft.arabicreference.content.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleTagLinkRepository extends JpaRepository<ArticleTagLinkEntity, ArticleTagLinkEntity.Key> {

    List<ArticleTagLinkEntity> findByArticleId(UUID articleId);

    boolean existsByArticleIdAndTagId(UUID articleId, UUID tagId);
}
