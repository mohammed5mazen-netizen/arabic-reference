package com.mrsoft.arabicreference.ai.infrastructure;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AiUsageRepository extends JpaRepository<AiUsageEntity, UUID> {

    long countByStatus(String status);

    @Query("select coalesce(avg(usage.latencyMs), 0) from AiUsageEntity usage")
    double averageLatency();
}
