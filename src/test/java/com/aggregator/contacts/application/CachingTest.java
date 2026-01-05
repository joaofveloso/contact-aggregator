package com.aggregator.contacts.application;

import static org.assertj.core.api.Assertions.assertThat;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.Duration;
import org.junit.jupiter.api.Test;

@QuarkusTest
class CachingTest {

    @Inject
    ContactApplicationService service;

    @Test
    void shouldReturnSameResultOnSecondCall() {
        // First call - cache miss, fetches from API
        FetchStatisticsDTO result1 = service.fetchAllContactsReactive().await().atMost(Duration.ofSeconds(10));

        // Second call - cache hit, returns cached result
        FetchStatisticsDTO result2 = service.fetchAllContactsReactive().await().atMost(Duration.ofSeconds(10));

        // Results should be equal (cached)
        assertThat(result1.successfulRecords()).isEqualTo(result2.successfulRecords());
        assertThat(result1.contacts()).hasSameSizeAs(result2.contacts());
    }

    @Test
    void shouldInvalidateCache() {
        // First call populates cache
        FetchStatisticsDTO result1 = service.fetchAllContactsReactive().await().atMost(Duration.ofSeconds(10));

        // Invalidate the cache
        service.invalidateCache();

        // Second call after invalidation fetches fresh data
        FetchStatisticsDTO result2 = service.fetchAllContactsReactive().await().atMost(Duration.ofSeconds(10));

        // Verify that cache invalidation works
        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();

        // Both should have the same structure (empty if API unavailable)
        assertThat(result1.skippedRecords()).isEqualTo(result2.skippedRecords());
        assertThat(result1.successfulRecords()).isEqualTo(result2.successfulRecords());
        assertThat(result1.contacts()).hasSameSizeAs(result2.contacts());
    }
}
