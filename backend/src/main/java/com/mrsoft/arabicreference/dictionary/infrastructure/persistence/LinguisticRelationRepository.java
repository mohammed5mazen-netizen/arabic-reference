package com.mrsoft.arabicreference.dictionary.infrastructure.persistence;

import com.mrsoft.arabicreference.dictionary.domain.RelationType;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LinguisticRelationRepository extends JpaRepository<LinguisticRelationEntity, UUID> {

    @Query("""
            select relation from LinguisticRelationEntity relation
            where relation.sourceEntryId = :entryId or relation.targetEntryId = :entryId
            """)
    List<LinguisticRelationEntity> findTouching(@Param("entryId") UUID entryId);

    List<LinguisticRelationEntity> findBySourceEntryId(UUID sourceEntryId);

    @Query("""
            select count(relation) from LinguisticRelationEntity relation
            where relation.relationType = :type
              and ((relation.sourceSenseId = :left and relation.targetSenseId = :right)
                or (relation.sourceSenseId = :right and relation.targetSenseId = :left))
            """)
    long countSymmetric(@Param("type") RelationType type, @Param("left") UUID left, @Param("right") UUID right);

    @Query("""
            select count(relation) from LinguisticRelationEntity relation
            where relation.relationType = :type
              and relation.sourceEntryId = :source
              and relation.targetEntryId = :target
            """)
    long countDerivation(@Param("type") RelationType type, @Param("source") UUID source, @Param("target") UUID target);
}
