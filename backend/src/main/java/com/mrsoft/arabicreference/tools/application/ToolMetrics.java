package com.mrsoft.arabicreference.tools.application;

import com.mrsoft.arabicreference.tools.application.ToolViews.ToolCount;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Aggregate tool counters. Raw visitor input is not logged.
 */
@Component
public class ToolMetrics {

    private static final Logger log = LoggerFactory.getLogger(ToolMetrics.class);

    private final MeterRegistry meters;
    private final ConcurrentHashMap<String, AtomicLong> calls = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> empty = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> errors = new ConcurrentHashMap<>();

    public ToolMetrics(MeterRegistry meters) {
        this.meters = meters;
    }

    public Timer.Sample start() {
        return Timer.start(meters);
    }

    public void success(String tool, boolean emptyResult, Timer.Sample sample) {
        calls.computeIfAbsent(tool, key -> new AtomicLong()).incrementAndGet();
        meters.counter("tools.calls", "tool", tool).increment();
        if (emptyResult) {
            empty.computeIfAbsent(tool, key -> new AtomicLong()).incrementAndGet();
            meters.counter("tools.zero_results", "tool", tool).increment();
        }
        long nanos = sample.stop(meters.timer("tools.duration", "tool", tool));
        log.info("tool completed tool={} empty={} durationMs={}", tool, emptyResult, nanos / 1_000_000);
    }

    public void failure(String tool, Timer.Sample sample) {
        errors.computeIfAbsent(tool, key -> new AtomicLong()).incrementAndGet();
        meters.counter("tools.errors", "tool", tool).increment();
        sample.stop(meters.timer("tools.duration", "tool", tool));
        log.info("tool failed tool={}", tool);
    }

    public List<ToolCount> snapshot() {
        List<ToolCount> counts = new ArrayList<>();
        calls.forEach((tool, value) -> counts.add(new ToolCount(
                tool,
                value.get(),
                empty.getOrDefault(tool, new AtomicLong()).get(),
                errors.getOrDefault(tool, new AtomicLong()).get())));
        return counts;
    }
}
