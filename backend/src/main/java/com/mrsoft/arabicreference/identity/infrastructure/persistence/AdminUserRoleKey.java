package com.mrsoft.arabicreference.identity.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class AdminUserRoleKey implements Serializable {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "role_id", nullable = false)
    private UUID roleId;

    public AdminUserRoleKey() {
    }

    public AdminUserRoleKey(UUID userId, UUID roleId) {
        this.userId = userId;
        this.roleId = roleId;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getRoleId() {
        return roleId;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof AdminUserRoleKey key && Objects.equals(userId, key.userId) && Objects.equals(roleId, key.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, roleId);
    }
}
