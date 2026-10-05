package com.mrsoft.arabicreference.content.infrastructure.persistence;

import com.mrsoft.arabicreference.content.domain.KnowledgeOwnerType;
import com.mrsoft.arabicreference.content.domain.KnowledgeTargetType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeRelationRepository extends JpaRepository<KnowledgeRelationEntity, UUID> {

    List<KnowledgeRelationEntity> findByOwnerTypeAndOwnerId(KnowledgeOwnerType ownerType, UUID ownerId);

    List<KnowledgeRelationEntity> findByTargetTypeAndTargetId(KnowledgeTargetType targetType, UUID targetId);

    boolean existsByOwnerTypeAndOwnerIdAndTargetTypeAndTargetId(
            KnowledgeOwnerType ownerType,
            UUID ownerId,
            KnowledgeTargetType targetType,
            UUID targetId);
}
