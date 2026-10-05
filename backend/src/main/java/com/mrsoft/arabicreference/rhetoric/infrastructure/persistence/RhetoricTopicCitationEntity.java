package com.mrsoft.arabicreference.rhetoric.infrastructure.persistence;

import com.mrsoft.arabicreference.shared.infrastructure.persistence.OwnerCitationEntity;
import com.mrsoft.arabicreference.shared.infrastructure.persistence.OwnerCitationKey;
import jakarta.persistence.Entity;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "rhetoric_topic_citation")
@IdClass(OwnerCitationKey.class)
public class RhetoricTopicCitationEntity extends OwnerCitationEntity {
    public RhetoricTopicCitationEntity() {}
    public RhetoricTopicCitationEntity(UUID ownerId, UUID citationId) { super(ownerId, citationId); }
}
