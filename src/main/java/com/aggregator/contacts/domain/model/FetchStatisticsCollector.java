package com.aggregator.contacts.domain.model;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FetchStatisticsCollector {
    private static final Logger LOG = LoggerFactory.getLogger(FetchStatisticsCollector.class);

    private final AtomicInteger successfulRecords = new AtomicInteger(0);
    private final AtomicInteger skippedRecords = new AtomicInteger(0);
    private final AtomicInteger successfulPages = new AtomicInteger(0);
    private final AtomicInteger skippedPages = new AtomicInteger(0);
    private final AtomicLong circuitBreakerTrippedCount = new AtomicLong(0);

    public void incrementSuccessfulRecords(int count) {
        successfulRecords.addAndGet(count);
    }

    public void incrementSkippedRecords() {
        skippedRecords.incrementAndGet();
    }

    public void incrementSuccessfulPages() {
        successfulPages.incrementAndGet();
    }

    public void incrementSkippedPages() {
        skippedPages.incrementAndGet();
    }

    public void incrementCircuitBreakerTripped() {
        long newCount = circuitBreakerTrippedCount.incrementAndGet();
        LOG.info("Circuit breaker tripped, total trips: {}", newCount);
    }

    public int successfulRecords() {
        return successfulRecords.get();
    }

    public int skippedRecords() {
        return skippedRecords.get();
    }

    public int successfulPages() {
        return successfulPages.get();
    }

    public int skippedPages() {
        return skippedPages.get();
    }

    public long circuitBreakerTrippedCount() {
        return circuitBreakerTrippedCount.get();
    }
}
