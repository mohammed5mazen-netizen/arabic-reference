package com.mrsoft.arabicreference.morphology.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface MorphologyStateRepository extends JpaRepository<MorphologyStateEntity, Short> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select state from MorphologyStateEntity state where state.id = 1")
    Optional<MorphologyStateEntity> lock();
}
