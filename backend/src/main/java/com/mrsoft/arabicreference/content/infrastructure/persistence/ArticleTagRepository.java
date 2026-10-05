package com.mrsoft.arabicreference.content.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleTagRepository extends JpaRepository<ArticleTagEntity, UUID> {

    Optional<ArticleTagEntity> findByNormalized(String normalized);
}
