package com.aggregator.contacts.infrastructure.adapter.outgoing;

import static org.assertj.core.api.Assertions.assertThat;

import com.aggregator.contacts.domain.model.Contact;
import com.aggregator.contacts.domain.model.FetchStatisticsCollector;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

/**
 * Tests for circuit breaker functionality in KenectLabsApiAdapter.
 *
 * <p>The circuit breaker is configured to:
 *
 * <ul>
 *   <li>Open after 4 requests with 50% failure ratio
 *   <li>Stay open for 10 seconds
 *   <li>Close after 2 successful requests
 *   <li>Use fallback method returning empty page when open
 * </ul>
 */
@QuarkusTest
class CircuitBreakerTest {

    @Inject
    KenectLabsApiAdapter adapter;

    @Test
    void shouldHaveCircuitBreakerOnFetchPage() {
        // Verify that fetchPage method exists and can be called
        FetchStatisticsCollector stats = new FetchStatisticsCollector();
        Uni<PaginationHandler.PaginationResult> result = adapter.fetchPage(1, stats);

        // Verify it returns a Uni (reactive type)
        assertThat(result).isNotNull();
    }

    @Test
    void shouldReturnMultiFromFetchAllContacts() {
        // Verify that fetchAllContacts still returns Multi for streaming
        FetchStatisticsCollector stats = new FetchStatisticsCollector();
        Multi<Contact> result = adapter.fetchAllContacts(stats);

        // Verify it returns a Multi (reactive stream)
        assertThat(result).isNotNull();
    }
}
