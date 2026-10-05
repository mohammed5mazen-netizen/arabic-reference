package com.mrsoft.arabicreference.rhetoric.infrastructure.persistence;

import com.mrsoft.arabicreference.rhetoric.domain.RhetoricComponentKind;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "rhetoric_component")
public class RhetoricComponentEntity {
    @Id
    private UUID id;
    @Column(name = "device_id", nullable = false)
    private UUID deviceId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RhetoricComponentKind kind;
    @Column(nullable = false, length = 160)
    private String heading;
    @Column(nullable = false, length = 4000)
    private String body;
    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getDeviceId() { return deviceId; }
    public void setDeviceId(UUID deviceId) { this.deviceId = deviceId; }
    public RhetoricComponentKind getKind() { return kind; }
    public void setKind(RhetoricComponentKind kind) { this.kind = kind; }
    public String getHeading() { return heading; }
    public void setHeading(String heading) { this.heading = heading; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
}
