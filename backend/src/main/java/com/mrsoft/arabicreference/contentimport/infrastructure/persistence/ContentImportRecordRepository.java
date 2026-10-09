package com.mrsoft.arabicreference.contentimport.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentImportRecordRepository extends JpaRepository<ContentImportRecordEntity, UUID> {
    Optional<ContentImportRecordEntity> findBySourceIdAndSourceRecordKey(UUID sourceId, String sourceRecordKey);
}
