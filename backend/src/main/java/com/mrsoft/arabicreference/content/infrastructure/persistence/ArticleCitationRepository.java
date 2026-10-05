package com.mrsoft.arabicreference.content.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleCitationRepository extends JpaRepository<ArticleCitationEntity, UUID> {

    List<ArticleCitationEntity> findByArticleId(UUID articleId);

    boolean existsByArticleIdAndSectionIdAndCitationId(UUID articleId, UUID sectionId, UUID citationId);

    boolean existsByArticleIdAndSectionIdIsNullAndCitationId(UUID articleId, UUID citationId);
}
