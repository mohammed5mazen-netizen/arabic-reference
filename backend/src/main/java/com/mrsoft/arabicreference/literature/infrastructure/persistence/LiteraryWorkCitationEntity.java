package com.mrsoft.arabicreference.literature.infrastructure.persistence;

import com.mrsoft.arabicreference.shared.infrastructure.persistence.OwnerCitationEntity;
import com.mrsoft.arabicreference.shared.infrastructure.persistence.OwnerCitationKey;
import jakarta.persistence.Entity;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "literary_work_citation")
@IdClass(OwnerCitationKey.class)
public class LiteraryWorkCitationEntity extends OwnerCitationEntity {

    public LiteraryWorkCitationEntity() {
    }

    public LiteraryWorkCitationEntity(UUID ownerId, UUID citationId) {
        super(ownerId, citationId);
    }
}
