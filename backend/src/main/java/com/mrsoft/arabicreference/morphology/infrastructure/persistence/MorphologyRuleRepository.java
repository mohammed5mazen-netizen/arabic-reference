package com.mrsoft.arabicreference.morphology.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MorphologyRuleRepository extends JpaRepository<MorphologyRuleEntity, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select rule from MorphologyRuleEntity rule where rule.code = :code")
    Optional<MorphologyRuleEntity> lockByCode(@Param("code") String code);
}
