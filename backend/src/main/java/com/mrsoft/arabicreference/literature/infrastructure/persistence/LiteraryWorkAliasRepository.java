package com.mrsoft.arabicreference.literature.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LiteraryWorkAliasRepository extends JpaRepository<LiteraryWorkAliasEntity, UUID> {

    List<LiteraryWorkAliasEntity> findByWorkIdOrderByAliasAsc(UUID workId);

    boolean existsByWorkIdAndNormalized(UUID workId, String normalized);
}
