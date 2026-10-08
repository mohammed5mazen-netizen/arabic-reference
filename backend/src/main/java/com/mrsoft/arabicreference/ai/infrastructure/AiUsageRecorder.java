package com.mrsoft.arabicreference.ai.infrastructure;

import com.mrsoft.arabicreference.ai.application.AiUsagePort;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AiUsageRecorder implements AiUsagePort {

    private static final Logger log = LoggerFactory.getLogger(AiUsageRecorder.class);

    private final AiUsageRepository repository;
    private final TimeProvider time;

    public AiUsageRecorder(AiUsageRepository repository, TimeProvider time) {
        this.repository = repository;
        this.time = time;
    }

    @Override
    public void record(UsageRecord record) {
        try {
            AiUsageEntity entity = new AiUsageEntity();
            entity.setId(UUID.randomUUID());
            entity.setRequestedAt(time.now());
            entity.setProvider(record.provider());
            entity.setModel(record.model());
            entity.setStatus(record.status());
            entity.setLatencyMs(Math.max(record.latencyMs(), 0));
            entity.setInputTokens(record.inputTokens());
            entity.setOutputTokens(record.outputTokens());
            entity.setEvidenceCount(Math.max(record.evidenceCount(), 0));
            repository.save(entity);
        } catch (RuntimeException exception) {
            log.warn("Assistant usage was not stored");
        }
    }

    @Override
    public UsageSnapshot snapshot() {
        long grounded = repository.countByStatus("GROUNDED");
        long partial = repository.countByStatus("PARTIALLY_GROUNDED");
        long insufficient = repository.countByStatus("INSUFFICIENT_EVIDENCE");
        long errors = repository.countByStatus("PROVIDER_ERROR");
        return new UsageSnapshot(grounded + partial + insufficient + errors, grounded, partial, insufficient, errors, repository.averageLatency());
    }
}
