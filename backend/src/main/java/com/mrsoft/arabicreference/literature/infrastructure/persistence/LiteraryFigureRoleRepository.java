package com.mrsoft.arabicreference.literature.infrastructure.persistence;

import com.mrsoft.arabicreference.literature.domain.LiteraryRole;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LiteraryFigureRoleRepository extends JpaRepository<LiteraryFigureRoleEntity, LiteraryFigureRoleEntity.Key> {

    List<LiteraryFigureRoleEntity> findByFigureIdOrderByRoleAsc(UUID figureId);

    boolean existsByFigureIdAndRole(UUID figureId, LiteraryRole role);
}
