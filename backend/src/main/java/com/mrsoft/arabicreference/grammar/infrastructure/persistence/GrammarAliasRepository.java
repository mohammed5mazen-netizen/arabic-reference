package com.mrsoft.arabicreference.grammar.infrastructure.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GrammarAliasRepository extends JpaRepository<GrammarConceptAliasEntity, UUID> {
    List<GrammarConceptAliasEntity> findByConceptIdOrderByAliasOriginalAsc(UUID conceptId);
    boolean existsByConceptIdAndAliasNormalized(UUID conceptId, String aliasNormalized);
}
