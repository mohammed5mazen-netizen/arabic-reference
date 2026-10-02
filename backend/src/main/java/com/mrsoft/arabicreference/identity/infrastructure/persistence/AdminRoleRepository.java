package com.mrsoft.arabicreference.identity.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AdminRoleRepository extends JpaRepository<AdminRoleEntity, UUID> {

    Optional<AdminRoleEntity> findByCode(String code);

    boolean existsByCode(String code);

    @Query(value = "SELECT id FROM admin_role WHERE code = 'PLATFORM_OWNER' FOR UPDATE", nativeQuery = true)
    UUID lockPlatformOwnerRole();
}
