package com.mrsoft.arabicreference.identity.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "admin_permission")
public class AdminPermissionEntity {

    @Id
    @Column(length = 80)
    private String code;

    @Column(nullable = false, length = 240)
    private String description;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof AdminPermissionEntity permission && Objects.equals(code, permission.code);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(code);
    }
}
