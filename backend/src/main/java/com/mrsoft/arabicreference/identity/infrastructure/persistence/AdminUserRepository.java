package com.mrsoft.arabicreference.identity.infrastructure.persistence;

import com.mrsoft.arabicreference.identity.domain.AccountStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

public interface AdminUserRepository extends JpaRepository<AdminUserEntity, UUID> {

    Optional<AdminUserEntity> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    long countByStatus(AccountStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select user from AdminUserEntity user where user.id = :id")
    Optional<AdminUserEntity> lockById(@Param("id") UUID id);

    @Query(value = """
            SELECT count(*)
            FROM admin_user users
            JOIN admin_user_role links ON links.user_id = users.id
            JOIN admin_role roles ON roles.id = links.role_id
            WHERE roles.code = 'PLATFORM_OWNER'
            """, nativeQuery = true)
    long countPlatformOwners();

    @Query(value = """
            SELECT count(*)
            FROM admin_user users
            JOIN admin_user_role links ON links.user_id = users.id
            JOIN admin_role roles ON roles.id = links.role_id
            WHERE roles.code = 'PLATFORM_OWNER'
              AND users.status = 'ACTIVE'
              AND users.id <> :userId
            """, nativeQuery = true)
    long countOtherActiveOwners(@Param("userId") UUID userId);
}
