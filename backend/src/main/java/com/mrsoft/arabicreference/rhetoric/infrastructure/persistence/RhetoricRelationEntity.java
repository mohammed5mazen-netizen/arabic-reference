package com.mrsoft.arabicreference.rhetoric.infrastructure.persistence;

import com.mrsoft.arabicreference.rhetoric.domain.RhetoricRelationKind;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "rhetoric_relation")
public class RhetoricRelationEntity {
    @Id
    private UUID id;
    @Column(name = "source_device_id", nullable = false)
    private UUID sourceDeviceId;
    @Column(name = "target_device_id", nullable = false)
    private UUID targetDeviceId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private RhetoricRelationKind kind;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getSourceDeviceId() { return sourceDeviceId; }
    public void setSourceDeviceId(UUID sourceDeviceId) { this.sourceDeviceId = sourceDeviceId; }
    public UUID getTargetDeviceId() { return targetDeviceId; }
    public void setTargetDeviceId(UUID targetDeviceId) { this.targetDeviceId = targetDeviceId; }
    public RhetoricRelationKind getKind() { return kind; }
    public void setKind(RhetoricRelationKind kind) { this.kind = kind; }
}
