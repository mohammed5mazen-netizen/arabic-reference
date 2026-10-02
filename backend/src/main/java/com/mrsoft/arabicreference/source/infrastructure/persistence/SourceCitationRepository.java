package com.mrsoft.arabicreference.source.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceCitationRepository extends JpaRepository<SourceCitationEntity, UUID> {

    List<SourceCitationEntity> findByIdIn(Collection<UUID> ids);

    List<SourceCitationEntity> findBySourceId(UUID sourceId);
}
