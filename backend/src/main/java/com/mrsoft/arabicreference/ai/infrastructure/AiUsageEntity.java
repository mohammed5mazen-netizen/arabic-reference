package com.mrsoft.arabicreference.ai.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ai_usage")
public class AiUsageEntity {

    @Id
    private UUID id;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(nullable = false, length = 40)
    private String provider;

    @Column(nullable = false, length = 80)
    private String model;

    @Column(nullable = false, length = 40)
    private String status;

    @Column(name = "latency_ms", nullable = false)
    private int latencyMs;

    @Column(name = "input_tokens")
    private Integer inputTokens;

    @Column(name = "output_tokens")
    private Integer outputTokens;

    @Column(name = "evidence_count", nullable = false)
    private int evidenceCount;

    public void setId(UUID id) {
        this.id = id;
    }

    public void setRequestedAt(Instant requestedAt) {
        this.requestedAt = requestedAt;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setLatencyMs(int latencyMs) {
        this.latencyMs = latencyMs;
    }

    public void setInputTokens(Integer inputTokens) {
        this.inputTokens = inputTokens;
    }

    public void setOutputTokens(Integer outputTokens) {
        this.outputTokens = outputTokens;
    }

    public void setEvidenceCount(int evidenceCount) {
        this.evidenceCount = evidenceCount;
    }
}
