package com.mrsoft.arabicreference.identity.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminPermissionRepository extends JpaRepository<AdminPermissionEntity, String> {
}
