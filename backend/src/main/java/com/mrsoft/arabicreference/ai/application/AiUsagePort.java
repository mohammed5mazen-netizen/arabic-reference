package com.mrsoft.arabicreference.ai.application;

public interface AiUsagePort {

    void record(UsageRecord record);

    UsageSnapshot snapshot();

    record UsageRecord(String provider, String model, String status, int latencyMs, Integer inputTokens, Integer outputTokens, int evidenceCount) {
    }

    record UsageSnapshot(long requests, long grounded, long partial, long insufficient, long providerErrors, double averageLatencyMs) {
    }
}
