package com.mrsoft.arabicreference.literature.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LiteraryExcerptRepository extends JpaRepository<LiteraryExcerptEntity, UUID> {

    List<LiteraryExcerptEntity> findByWorkIdOrderByDisplayOrderAsc(UUID workId);
}
