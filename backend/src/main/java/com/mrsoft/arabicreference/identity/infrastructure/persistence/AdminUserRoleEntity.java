package com.mrsoft.arabicreference.identity.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "admin_user_role")
public class AdminUserRoleEntity {

    @EmbeddedId
    private AdminUserRoleKey id;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt;

    public AdminUserRoleKey getId() {
        return id;
    }

    public void setId(AdminUserRoleKey id) {
        this.id = id;
    }

    public Instant getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(Instant assignedAt) {
        this.assignedAt = assignedAt;
    }
}
