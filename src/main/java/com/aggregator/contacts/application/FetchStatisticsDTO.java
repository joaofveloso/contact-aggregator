package com.aggregator.contacts.application;

import com.aggregator.contacts.domain.model.Contact;
import com.aggregator.contacts.domain.model.FetchStatisticsCollector;
import java.util.List;

public record FetchStatisticsDTO(
        int successfulRecords,
        int skippedRecords,
        int successfulPages,
        int skippedPages,
        long circuitBreakerTrippedCount,
        List<Contact> contacts) {
    /** Canonical constructor with default values. */
    public FetchStatisticsDTO() {
        this(0, 0, 0, 0, 0, List.of());
    }

    public static FetchStatisticsDTO from(FetchStatisticsCollector collector, List<Contact> contacts) {
        return new FetchStatisticsDTO(
                collector.successfulRecords(),
                collector.skippedRecords(),
                collector.successfulPages(),
                collector.skippedPages(),
                collector.circuitBreakerTrippedCount(),
                contacts);
    }
}
