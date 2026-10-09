package com.mrsoft.arabicreference.contentimport.infrastructure.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentImportBatchRepository extends JpaRepository<ContentImportBatchEntity, UUID> {
}
