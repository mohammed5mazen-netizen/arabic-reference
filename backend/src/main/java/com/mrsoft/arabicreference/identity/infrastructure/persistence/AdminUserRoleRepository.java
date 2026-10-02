package com.mrsoft.arabicreference.identity.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminUserRoleRepository extends JpaRepository<AdminUserRoleEntity, AdminUserRoleKey> {

    @Modifying(flushAutomatically = true, clearAutomatically = false)
    @Query("delete from AdminUserRoleEntity link where link.id.userId = :userId")
    void deleteForUser(@Param("userId") UUID userId);

    @Query(value = """
            SELECT roles.code
            FROM admin_user_role links
            JOIN admin_role roles ON roles.id = links.role_id
            WHERE links.user_id = :userId
            ORDER BY roles.code
            """, nativeQuery = true)
    List<String> findRoleCodes(@Param("userId") UUID userId);

    @Query("""
            select role.id, role.code, role.name
            from AdminRoleEntity role, AdminUserRoleEntity link
            where link.id.roleId = role.id and link.id.userId = :userId
            order by role.code
            """)
    List<Object[]> findRoleSummaries(@Param("userId") UUID userId);

    @Query(value = """
            SELECT DISTINCT grants.permission_code
            FROM admin_user_role links
            JOIN admin_role_permission grants ON grants.role_id = links.role_id
            WHERE links.user_id = :userId
            ORDER BY grants.permission_code
            """, nativeQuery = true)
    List<String> findPermissionCodes(@Param("userId") UUID userId);

    @Query(value = """
            SELECT count(*)
            FROM admin_user_role links
            JOIN admin_role roles ON roles.id = links.role_id
            WHERE links.user_id = :userId AND roles.code = :code
            """, nativeQuery = true)
    long countRole(@Param("userId") UUID userId, @Param("code") String code);

    @Query(value = "SELECT count(*) FROM admin_user_role WHERE user_id = :userId AND role_id = :roleId", nativeQuery = true)
    long countAssignment(@Param("userId") UUID userId, @Param("roleId") UUID roleId);
}
