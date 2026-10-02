package com.mrsoft.arabicreference.identity.application;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class StaffViews {

    private StaffViews() {
    }

    public record TokenPair(
            String accessToken,
            Instant accessTokenExpiresAt,
            String refreshToken,
            Instant refreshTokenExpiresAt,
            boolean mustChangePassword) {
    }

    public record SessionView(
            UUID id,
            String username,
            String displayName,
            boolean mustChangePassword,
            List<String> roles,
            List<String> permissions) {
    }

    public record RoleRef(UUID id, String code, String name) {
    }

    public record AdminUserView(
            UUID id,
            String username,
            String email,
            String displayName,
            String status,
            boolean mustChangePassword,
            int failedLoginAttempts,
            Instant lockedUntil,
            Instant lastLoginAt,
            Instant passwordChangedAt,
            Instant createdAt,
            Instant updatedAt,
            long version,
            List<RoleRef> roles) {
    }

    public record CreatedAdminUser(AdminUserView user, String temporaryPassword) {
    }

    public record ResetPasswordResult(AdminUserView user, String temporaryPassword) {
    }

    public record RoleView(
            UUID id,
            String code,
            String name,
            String description,
            boolean systemRole,
            long version,
            List<String> permissions) {
    }

    public record PermissionView(String code, String description) {
    }

    public record AuditView(
            UUID id,
            UUID actorId,
            String eventType,
            String targetType,
            String targetId,
            Instant occurredAt,
            String traceId,
            java.util.Map<String, String> metadata) {
    }

    public record PageResult<T>(List<T> items, int page, int size, long total) {
    }

    public record UserCounts(long total, long active, long locked) {
    }

    public record DashboardView(UserCounts users, List<AuditView> recentAudit) {
    }
}
