package com.mrsoft.arabicreference.identity.application;

import com.mrsoft.arabicreference.identity.application.StaffViews.AdminUserView;
import com.mrsoft.arabicreference.identity.application.StaffViews.AuditView;
import com.mrsoft.arabicreference.identity.application.StaffViews.RoleRef;
import com.mrsoft.arabicreference.identity.application.StaffViews.RoleView;
import com.mrsoft.arabicreference.identity.infrastructure.persistence.AdminAuditEventEntity;
import com.mrsoft.arabicreference.identity.infrastructure.persistence.AdminRoleEntity;
import com.mrsoft.arabicreference.identity.infrastructure.persistence.AdminUserEntity;
import com.mrsoft.arabicreference.identity.infrastructure.persistence.AdminUserRoleRepository;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class StaffDirectory {

    private final AdminUserRoleRepository links;

    public StaffDirectory(AdminUserRoleRepository links) {
        this.links = links;
    }

    public AdminUserView user(AdminUserEntity entity) {
        List<RoleRef> roles = links.findRoleSummaries(entity.getId()).stream()
                .map(row -> new RoleRef((java.util.UUID) row[0], (String) row[1], (String) row[2]))
                .toList();
        return new AdminUserView(
                entity.getId(),
                entity.getUsername(),
                entity.getEmail(),
                entity.getDisplayName(),
                entity.getStatus().name(),
                entity.isMustChangePassword(),
                entity.getFailedLoginAttempts(),
                entity.getLockedUntil(),
                entity.getLastLoginAt(),
                entity.getPasswordChangedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion(),
                roles);
    }

    public RoleView role(AdminRoleEntity entity) {
        List<String> permissions = entity.getPermissions().stream()
                .map(permission -> permission.getCode())
                .sorted()
                .toList();
        return new RoleView(
                entity.getId(),
                entity.getCode(),
                entity.getName(),
                entity.getDescription(),
                entity.isSystemRole(),
                entity.getVersion(),
                permissions);
    }

    public AuditView audit(AdminAuditEventEntity entity) {
        return new AuditView(
                entity.getId(),
                entity.getActorId(),
                entity.getEventType().name(),
                entity.getTargetType(),
                entity.getTargetId(),
                entity.getOccurredAt(),
                entity.getTraceId(),
                entity.getMetadata());
    }
}
