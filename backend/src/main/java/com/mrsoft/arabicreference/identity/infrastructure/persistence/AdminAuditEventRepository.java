package com.mrsoft.arabicreference.identity.infrastructure.persistence;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminAuditEventRepository extends JpaRepository<AdminAuditEventEntity, UUID> {

    Page<AdminAuditEventEntity> findAllByOrderByOccurredAtDesc(Pageable pageable);
}
