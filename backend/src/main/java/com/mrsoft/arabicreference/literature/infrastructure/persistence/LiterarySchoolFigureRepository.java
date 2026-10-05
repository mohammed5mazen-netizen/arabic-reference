package com.mrsoft.arabicreference.literature.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LiterarySchoolFigureRepository extends JpaRepository<LiterarySchoolFigureEntity, LiterarySchoolFigureEntity.Key> {

    List<LiterarySchoolFigureEntity> findByFigureId(UUID figureId);

    List<LiterarySchoolFigureEntity> findBySchoolId(UUID schoolId);

    boolean existsBySchoolIdAndFigureId(UUID schoolId, UUID figureId);
}
