package com.mrsoft.arabicreference.grammar.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GrammarTokenRepository extends JpaRepository<GrammarTokenEntity, UUID> {
    List<GrammarTokenEntity> findByAnnotationIdOrderByPositionAsc(UUID annotationId);
    void deleteByAnnotationId(UUID annotationId);
}
