package com.aggregator.contacts.domain.model;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class FetchStatisticsCollectorTest {

    @Test
    void shouldInitializeWithZeroValues() {
        FetchStatisticsCollector collector = new FetchStatisticsCollector();

        assertThat(collector.successfulRecords()).isZero();
        assertThat(collector.skippedRecords()).isZero();
        assertThat(collector.successfulPages()).isZero();
        assertThat(collector.skippedPages()).isZero();
        assertThat(collector.circuitBreakerTrippedCount()).isZero();
    }

    @Test
    void shouldIncrementSuccessfulRecords() {
        FetchStatisticsCollector collector = new FetchStatisticsCollector();

        collector.incrementSuccessfulRecords(5);
        assertThat(collector.successfulRecords()).isEqualTo(5);

        collector.incrementSuccessfulRecords(3);
        assertThat(collector.successfulRecords()).isEqualTo(8);
    }

    @Test
    void shouldIncrementSkippedRecords() {
        FetchStatisticsCollector collector = new FetchStatisticsCollector();

        collector.incrementSkippedRecords();
        assertThat(collector.skippedRecords()).isEqualTo(1);

        collector.incrementSkippedRecords();
        assertThat(collector.skippedRecords()).isEqualTo(2);
    }

    @Test
    void shouldIncrementSuccessfulPages() {
        FetchStatisticsCollector collector = new FetchStatisticsCollector();

        collector.incrementSuccessfulPages();
        assertThat(collector.successfulPages()).isEqualTo(1);

        collector.incrementSuccessfulPages();
        assertThat(collector.successfulPages()).isEqualTo(2);
    }

    @Test
    void shouldIncrementSkippedPages() {
        FetchStatisticsCollector collector = new FetchStatisticsCollector();

        collector.incrementSkippedPages();
        assertThat(collector.skippedPages()).isEqualTo(1);

        collector.incrementSkippedPages();
        assertThat(collector.skippedPages()).isEqualTo(2);
    }

    @Test
    void shouldIncrementCircuitBreakerTrippedCount() {
        FetchStatisticsCollector collector = new FetchStatisticsCollector();

        collector.incrementCircuitBreakerTripped();
        assertThat(collector.circuitBreakerTrippedCount()).isEqualTo(1);

        collector.incrementCircuitBreakerTripped();
        assertThat(collector.circuitBreakerTrippedCount()).isEqualTo(2);
    }
}
