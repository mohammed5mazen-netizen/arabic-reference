package com.mrsoft.arabicreference.spelling.infrastructure.persistence;

import com.mrsoft.arabicreference.shared.infrastructure.persistence.OwnerCitationKey;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpellingRuleCitationRepository extends JpaRepository<SpellingRuleCitationEntity, OwnerCitationKey> {

    boolean existsByOwnerIdAndCitationId(UUID ownerId, UUID citationId);

    List<SpellingRuleCitationEntity> findByOwnerId(UUID ownerId);
}
