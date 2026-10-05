package com.mrsoft.arabicreference.spelling.infrastructure.persistence;

import com.mrsoft.arabicreference.shared.infrastructure.persistence.OwnerCitationEntity;
import com.mrsoft.arabicreference.shared.infrastructure.persistence.OwnerCitationKey;
import jakarta.persistence.Entity;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "spelling_topic_citation")
@IdClass(OwnerCitationKey.class)
public class SpellingTopicCitationEntity extends OwnerCitationEntity {

    public SpellingTopicCitationEntity() {
    }

    public SpellingTopicCitationEntity(UUID ownerId, UUID citationId) {
        super(ownerId, citationId);
    }
}
