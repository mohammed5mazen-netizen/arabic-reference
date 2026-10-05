package com.mrsoft.arabicreference.spelling.infrastructure.persistence;

import com.mrsoft.arabicreference.shared.infrastructure.persistence.OwnerCitationKey;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpellingTopicCitationRepository extends JpaRepository<SpellingTopicCitationEntity, OwnerCitationKey> {

    boolean existsByOwnerIdAndCitationId(UUID ownerId, UUID citationId);

    List<SpellingTopicCitationEntity> findByOwnerId(UUID ownerId);
}
