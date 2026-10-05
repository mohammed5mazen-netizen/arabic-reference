package com.mrsoft.arabicreference.literature.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LiteraryFigureEraRepository extends JpaRepository<LiteraryFigureEraEntity, LiteraryFigureEraEntity.Key> {

    List<LiteraryFigureEraEntity> findByFigureId(UUID figureId);

    boolean existsByFigureIdAndEraId(UUID figureId, UUID eraId);
}
