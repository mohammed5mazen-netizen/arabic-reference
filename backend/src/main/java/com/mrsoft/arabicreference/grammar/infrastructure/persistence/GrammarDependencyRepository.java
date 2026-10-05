package com.mrsoft.arabicreference.grammar.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GrammarDependencyRepository extends JpaRepository<GrammarDependencyEntity, UUID> {
    List<GrammarDependencyEntity> findByAnnotationIdOrderByGovernorPositionAsc(UUID annotationId);
    void deleteByAnnotationId(UUID annotationId);
}
