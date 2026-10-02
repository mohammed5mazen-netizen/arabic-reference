package com.mrsoft.arabicreference.linguistics.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ContentRevisionRepository extends JpaRepository<ContentRevisionEntity, UUID> {

    @Query("""
            select coalesce(max(revision.revisionNumber), 0)
            from ContentRevisionEntity revision
            where revision.targetType = :targetType and revision.targetId = :targetId
            """)
    int maxNumber(@Param("targetType") String targetType, @Param("targetId") UUID targetId);

    List<ContentRevisionEntity> findByTargetTypeAndTargetIdOrderByRevisionNumberDesc(String targetType, UUID targetId);
}
