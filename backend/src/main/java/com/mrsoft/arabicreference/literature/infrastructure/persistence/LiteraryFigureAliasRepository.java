package com.mrsoft.arabicreference.literature.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LiteraryFigureAliasRepository extends JpaRepository<LiteraryFigureAliasEntity, UUID> {

    List<LiteraryFigureAliasEntity> findByFigureIdOrderByAliasAsc(UUID figureId);

    boolean existsByFigureIdAndNormalized(UUID figureId, String normalized);
}
