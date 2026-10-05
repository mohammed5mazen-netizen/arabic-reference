package com.mrsoft.arabicreference.literature.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LiteraryWorkFigureRepository extends JpaRepository<LiteraryWorkFigureEntity, LiteraryWorkFigureEntity.Key> {

    List<LiteraryWorkFigureEntity> findByWorkId(UUID workId);

    List<LiteraryWorkFigureEntity> findByFigureId(UUID figureId);

    boolean existsByWorkIdAndFigureId(UUID workId, UUID figureId);
}
