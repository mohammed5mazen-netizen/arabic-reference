package com.mrsoft.arabicreference.rhetoric.infrastructure.persistence;

import com.mrsoft.arabicreference.shared.infrastructure.persistence.OwnerCitationEntity;
import com.mrsoft.arabicreference.shared.infrastructure.persistence.OwnerCitationKey;
import jakarta.persistence.Entity;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "rhetoric_device_citation")
@IdClass(OwnerCitationKey.class)
public class RhetoricDeviceCitationEntity extends OwnerCitationEntity {
    public RhetoricDeviceCitationEntity() {}
    public RhetoricDeviceCitationEntity(UUID ownerId, UUID citationId) { super(ownerId, citationId); }
}
