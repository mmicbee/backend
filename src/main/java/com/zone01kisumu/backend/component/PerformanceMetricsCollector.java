package com.zone01kisumu.backend.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

import com.zone01kisumu.backend.dto.MonitoringDTOs.ApiPerformanceStats;

/**
 * Thread-safe collector for tracking API request counts, latencies, and percentile metrics.
 */
@Component
public class PerformanceMetricsCollector {

    private static final int MAX_LATENCY_SAMPLES = 1000;

    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);
    private final AtomicLong totalDurationMs = new AtomicLong(0);
    private final ConcurrentLinkedDeque<Long> latencySamples = new ConcurrentLinkedDeque<>();

    /**
     * Records an API request execution.
     *
     * @param durationMs Execution time in milliseconds.
     * @param isError    Whether the request resulted in an error (4xx or 5xx).
     */
    public void recordRequest(long durationMs, boolean isError) {
        totalRequests.incrementAndGet();
        totalDurationMs.addAndGet(durationMs);

        if (isError) {
            totalErrors.incrementAndGet();
        }

        latencySamples.add(durationMs);
        while (latencySamples.size() > MAX_LATENCY_SAMPLES) {
            latencySamples.poll();
        }
    }

    /**
     * Returns current aggregated API performance statistics.
     *
     * @return ApiPerformanceStats.
     */
    public ApiPerformanceStats getMetrics() {
        long requests = totalRequests.get();
        long errors = totalErrors.get();
        double errorRate = (requests > 0) ? ((double) errors / requests) * 100.0 : 0.0;
        double avgResponse = (requests > 0) ? ((double) totalDurationMs.get() / requests) : 0.0;

        List<Long> samples = new ArrayList<>(latencySamples);
        Collections.sort(samples);

        double p95 = getPercentile(samples, 95);
        double p99 = getPercentile(samples, 99);

        return ApiPerformanceStats.builder()
                .totalRequests(requests)
                .totalErrors(errors)
                .errorRatePercent(round(errorRate))
                .avgResponseTimeMs(round(avgResponse))
                .p95ResponseTimeMs(round(p95))
                .p99ResponseTimeMs(round(p99))
                .build();
    }

    /**
     * Resets telemetry counters (useful for testing or periodic baseline reset).
     */
    public void reset() {
        totalRequests.set(0);
        totalErrors.set(0);
        totalDurationMs.set(0);
        latencySamples.clear();
    }

    private double getPercentile(List<Long> sortedSamples, double percentile) {
        if (sortedSamples.isEmpty()) {
            return 0.0;
        }
        int index = (int) Math.ceil((percentile / 100.0) * sortedSamples.size()) - 1;
        index = Math.max(0, Math.min(index, sortedSamples.size() - 1));
        return sortedSamples.get(index);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
