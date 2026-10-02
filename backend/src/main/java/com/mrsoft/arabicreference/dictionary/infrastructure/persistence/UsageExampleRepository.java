package com.mrsoft.arabicreference.dictionary.infrastructure.persistence;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UsageExampleRepository extends JpaRepository<UsageExampleEntity, UUID> {

    List<UsageExampleEntity> findBySenseIdInOrderByDisplayOrderAsc(Collection<UUID> senseIds);

    @Query("select coalesce(max(example.displayOrder), 0) from UsageExampleEntity example where example.senseId = :senseId")
    int maxOrder(@Param("senseId") UUID senseId);
}
