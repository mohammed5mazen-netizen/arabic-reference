package com.mrsoft.arabicreference.ai.application;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AiMetrics {

    private static final Logger log = LoggerFactory.getLogger(AiMetrics.class);

    private final MeterRegistry meters;
    private final DistributionSummary evidence;

    public AiMetrics(MeterRegistry meters) {
        this.meters = meters;
        this.evidence = DistributionSummary.builder("ai.evidence.count").register(meters);
    }

    public Timer.Sample start() {
        return Timer.start(meters);
    }

    public void record(String status, int evidenceCount, Timer.Sample sample) {
        meters.counter("ai.requests", "status", status).increment();
        if ("GROUNDED".equals(status)) {
            meters.counter("ai.grounded").increment();
        } else if ("INSUFFICIENT_EVIDENCE".equals(status)) {
            meters.counter("ai.insufficient").increment();
        } else if ("PROVIDER_ERROR".equals(status)) {
            meters.counter("ai.provider.errors").increment();
        }
        evidence.record(evidenceCount);
        long nanos = sample.stop(meters.timer("ai.duration", "status", status));
        log.info("assistant completed status={} evidenceCount={} durationMs={}", status, evidenceCount, nanos / 1_000_000);
    }
}
