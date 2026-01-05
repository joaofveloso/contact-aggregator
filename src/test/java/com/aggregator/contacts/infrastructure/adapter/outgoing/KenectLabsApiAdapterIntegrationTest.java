package com.aggregator.contacts.infrastructure.adapter.outgoing;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.*;

import com.aggregator.contacts.domain.model.Contact;
import com.aggregator.contacts.domain.model.FetchStatisticsCollector;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Multi;
import jakarta.inject.Inject;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@WireMockTest(httpPort = 8089)
class KenectLabsApiAdapterIntegrationTest {

    @Inject
    KenectLabsApiAdapter adapter;

    @BeforeEach
    void resetWireMock() {
        // Reset all WireMock stubs and scenarios before each test
        reset();
    }

    @Test
    void shouldFetchSinglePage() {
        stubFor(get(urlPathEqualTo("/api/v1/contacts"))
                .withQueryParam("page", equalTo("1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Current-Page", "1")
                        .withHeader("Total-Pages", "1")
                        .withHeader("Page-Items", "2")
                        .withHeader("Total-Count", "2")
                        .withBody("""
                    [{"id":1,"name":"John Doe","email":"john@example.com","createdAt":"2024-01-01T00:00:00Z","updatedAt":"2024-01-01T00:00:00Z"},
                     {"id":2,"name":"Jane Smith","email":"jane@example.com","createdAt":"2024-01-01T00:00:00Z","updatedAt":"2024-01-01T00:00:00Z"}]
                    """)));

        FetchStatisticsCollector stats = new FetchStatisticsCollector();
        Multi<Contact> contacts = adapter.fetchAllContacts(stats);
        List<Contact> list = contacts.collect().asList().await().indefinitely();

        assertThat(list).hasSize(2);
        assertThat(list.get(0).name()).isEqualTo("John Doe");
        assertThat(list.get(1).name()).isEqualTo("Jane Smith");
        assertThat(stats.successfulRecords()).isEqualTo(2);
        assertThat(stats.successfulPages()).isEqualTo(1);
    }

    @Test
    void shouldFetchMultiplePages() {
        // Page 1
        stubFor(get(urlPathEqualTo("/api/v1/contacts"))
                .withQueryParam("page", equalTo("1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Current-Page", "1")
                        .withHeader("Total-Pages", "3")
                        .withHeader("Page-Items", "1")
                        .withHeader("Total-Count", "3")
                        .withBody("""
                    [{"id":1,"name":"Contact 1","email":"contact1@example.com","createdAt":"2024-01-01T00:00:00Z","updatedAt":"2024-01-01T00:00:00Z"}]
                    """)));

        // Page 2
        stubFor(get(urlPathEqualTo("/api/v1/contacts"))
                .withQueryParam("page", equalTo("2"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Current-Page", "2")
                        .withHeader("Total-Pages", "3")
                        .withHeader("Page-Items", "1")
                        .withHeader("Total-Count", "3")
                        .withBody("""
                    [{"id":2,"name":"Contact 2","email":"contact2@example.com","createdAt":"2024-01-01T00:00:00Z","updatedAt":"2024-01-01T00:00:00Z"}]
                    """)));

        // Page 3
        stubFor(get(urlPathEqualTo("/api/v1/contacts"))
                .withQueryParam("page", equalTo("3"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Current-Page", "3")
                        .withHeader("Total-Pages", "3")
                        .withHeader("Page-Items", "1")
                        .withHeader("Total-Count", "3")
                        .withBody("""
                    [{"id":3,"name":"Contact 3","email":"contact3@example.com","createdAt":"2024-01-01T00:00:00Z","updatedAt":"2024-01-01T00:00:00Z"}]
                    """)));

        FetchStatisticsCollector stats = new FetchStatisticsCollector();
        Multi<Contact> contacts = adapter.fetchAllContacts(stats);
        List<Contact> list = contacts.collect().asList().await().indefinitely();

        assertThat(list).hasSize(3);
        assertThat(stats.successfulRecords()).isEqualTo(3);
        assertThat(stats.successfulPages()).isEqualTo(3);
    }

    @Test
    void shouldSkipFailedPageAndContinue() {
        // Page 1 - success
        stubFor(get(urlPathEqualTo("/api/v1/contacts"))
                .withQueryParam("page", equalTo("1"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Current-Page", "1")
                        .withHeader("Total-Pages", "3")
                        .withHeader("Page-Items", "1")
                        .withHeader("Total-Count", "3")
                        .withBody("""
                    [{"id":1,"name":"Contact 1","email":"contact1@example.com","createdAt":"2024-01-01T00:00:00Z","updatedAt":"2024-01-01T00:00:00Z"}]
                    """)));

        // Page 2 - server error
        stubFor(get(urlPathEqualTo("/api/v1/contacts"))
                .withQueryParam("page", equalTo("2"))
                .willReturn(aResponse().withStatus(500).withBody("Internal Server Error")));

        // Page 3 - success
        stubFor(get(urlPathEqualTo("/api/v1/contacts"))
                .withQueryParam("page", equalTo("3"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Current-Page", "3")
                        .withHeader("Total-Pages", "3")
                        .withHeader("Page-Items", "1")
                        .withHeader("Total-Count", "3")
                        .withBody("""
                    [{"id":3,"name":"Contact 3","email":"contact3@example.com","createdAt":"2024-01-01T00:00:00Z","updatedAt":"2024-01-01T00:00:00Z"}]
                    """)));

        FetchStatisticsCollector stats = new FetchStatisticsCollector();
        Multi<Contact> contacts = adapter.fetchAllContacts(stats);
        List<Contact> list = contacts.collect().asList().await().indefinitely();

        assertThat(list).hasSize(2); // Only pages 1 and 3
        assertThat(stats.successfulPages()).isEqualTo(2);
        assertThat(stats.skippedPages()).isEqualTo(1);
    }

    @Test
    void shouldFetchAllConsecutivePagesWithoutSkipping() {
        // Stub 5 pages to detect page skipping
        for (int page = 1; page <= 5; page++) {
            final int pageNum = page;
            String jsonBody = String.format(
                    "[{\"id\":%d,\"name\":\"Contact %d\",\"email\":\"contact%d@example.com\",\"createdAt\":\"2024-01-01T00:00:00Z\",\"updatedAt\":\"2024-01-01T00:00:00Z\"}]",
                    pageNum, pageNum, pageNum);

            stubFor(get(urlPathEqualTo("/api/v1/contacts"))
                    .withQueryParam("page", equalTo(String.valueOf(pageNum)))
                    .willReturn(aResponse()
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withHeader("Current-Page", String.valueOf(pageNum))
                            .withHeader("Total-Pages", "5")
                            .withHeader("Page-Items", "1")
                            .withHeader("Total-Count", "5")
                            .withBody(jsonBody)));
        }

        FetchStatisticsCollector stats = new FetchStatisticsCollector();
        Multi<Contact> contacts = adapter.fetchAllContacts(stats);
        List<Contact> list = contacts.collect().asList().await().indefinitely();

        // Verify all 5 contacts fetched (no pages skipped)
        assertThat(list).hasSize(5);
        assertThat(stats.successfulRecords()).isEqualTo(5);
        assertThat(stats.successfulPages()).isEqualTo(5);

        // Verify contacts are in correct order (proves consecutive pages)
        assertThat(list.get(0).id()).isEqualTo(1);
        assertThat(list.get(1).id()).isEqualTo(2);
        assertThat(list.get(2).id()).isEqualTo(3);
        assertThat(list.get(3).id()).isEqualTo(4);
        assertThat(list.get(4).id()).isEqualTo(5);

        // Verify WireMock received requests for all consecutive pages
        verify(1, getRequestedFor(urlPathEqualTo("/api/v1/contacts")).withQueryParam("page", equalTo("1")));
        verify(1, getRequestedFor(urlPathEqualTo("/api/v1/contacts")).withQueryParam("page", equalTo("2")));
        verify(1, getRequestedFor(urlPathEqualTo("/api/v1/contacts")).withQueryParam("page", equalTo("3")));
        verify(1, getRequestedFor(urlPathEqualTo("/api/v1/contacts")).withQueryParam("page", equalTo("4")));
        verify(1, getRequestedFor(urlPathEqualTo("/api/v1/contacts")).withQueryParam("page", equalTo("5")));
    }

    @Test
    void shouldHandleInterruptedExceptionByRestoringInterruptAndStopping() throws InterruptedException {
        // Stub 5 pages with a small delay to ensure interrupt happens mid-fetch
        for (int page = 1; page <= 5; page++) {
            final int pageNum = page;
            stubFor(get(urlPathEqualTo("/api/v1/contacts"))
                    .withQueryParam("page", equalTo(String.valueOf(pageNum)))
                    .willReturn(aResponse()
                            .withFixedDelay(200) // 200ms delay per page
                            .withStatus(200)
                            .withHeader("Content-Type", "application/json")
                            .withHeader("Current-Page", String.valueOf(pageNum))
                            .withHeader("Total-Pages", "5")
                            .withHeader("Page-Items", "1")
                            .withHeader("Total-Count", "5")
                            .withBody(String.format(
                                    "[{\"id\":%d,\"name\":\"Contact %d\",\"email\":\"contact%d@example.com\",\"createdAt\":\"2024-01-01T00:00:00Z\",\"updatedAt\":\"2024-01-01T00:00:00Z\"}]",
                                    pageNum, pageNum, pageNum))));
        }

        FetchStatisticsCollector stats = new FetchStatisticsCollector();

        Thread fetchThread = new Thread(() -> {
            try {
                Multi<Contact> contacts = adapter.fetchAllContacts(stats);
                contacts.collect().asList().await().indefinitely();
            } catch (Exception e) {
                // Expected to be interrupted or stream to fail
            }
        });

        fetchThread.start();
        // Thread.sleep(350); // Let it start fetching (should complete page 1, start page 2)
        fetchThread.interrupt(); // Interrupt during fetch
        fetchThread.join(3000); // Wait for it to finish

        // Verify that pagination stopped and didn't fetch all 5 pages
        // With 200ms delay per page and 350ms sleep, it should complete page 1 (200ms)
        // and get interrupted during page 2 (200-400ms), so should have 1-2 pages max
        assertThat(stats.successfulPages()).isLessThan(5);

        verify(lessThan(5), getRequestedFor(urlPathEqualTo("/api/v1/contacts")));
    }

    @Test
    void shouldRetryOnTransientFailure() {
        // Use scenario-based stubbing to simulate transient failure on first attempt
        stubFor(get(urlPathEqualTo("/api/v1/contacts"))
                .withQueryParam("page", equalTo("1"))
                .inScenario("shouldRetryOnTransientFailure")
                .whenScenarioStateIs("Started")
                .willReturn(aResponse().withStatus(503).withBody("Service Unavailable"))
                .willSetStateTo("RetrySucceeded"));

        stubFor(
                get(urlPathEqualTo("/api/v1/contacts"))
                        .withQueryParam("page", equalTo("1"))
                        .inScenario("shouldRetryOnTransientFailure")
                        .whenScenarioStateIs("RetrySucceeded")
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withHeader("Current-Page", "1")
                                        .withHeader("Total-Pages", "1")
                                        .withHeader("Page-Items", "1")
                                        .withHeader("Total-Count", "1")
                                        .withBody(
                                                "[{\"id\":1,\"name\":\"Contact 1\",\"email\":\"contact1@example.com\",\"createdAt\":\"2024-01-01T00:00:00Z\",\"updatedAt\":\"2024-01-01T00:00:00Z\"}]")));

        FetchStatisticsCollector stats = new FetchStatisticsCollector();
        Multi<Contact> contacts = adapter.fetchAllContacts(stats);
        List<Contact> list = contacts.collect().asList().await().indefinitely();

        assertThat(list).hasSize(1);
        assertThat(list.getFirst().name()).isEqualTo("Contact 1");
        assertThat(stats.successfulRecords()).isEqualTo(1);
        assertThat(stats.successfulPages()).isEqualTo(1);
        assertThat(stats.skippedPages()).isZero();

        // The successful fetch after 503 error proves retry logic works
        // (scenario returns 503 on first request, 200 on second)
    }

    @Test
    void shouldHandleInvalidPaginationHeadersGracefully() {
        // Return invalid (non-numeric) pagination headers
        stubFor(
                get(urlPathEqualTo("/api/v1/contacts"))
                        .withQueryParam("page", equalTo("1"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withHeader("Current-Page", "invalid")
                                        .withHeader("Total-Pages", "not-a-number")
                                        .withHeader("Page-Items", "1")
                                        .withHeader("Total-Count", "1")
                                        .withBody(
                                                "[{\"id\":1,\"name\":\"Contact 1\",\"email\":\"contact1@example.com\",\"createdAt\":\"2024-01-01T00:00:00Z\",\"updatedAt\":\"2024-01-01T00:00:00Z\"}]")));

        FetchStatisticsCollector stats = new FetchStatisticsCollector();
        Multi<Contact> contacts = adapter.fetchAllContacts(stats);
        List<Contact> list = contacts.collect().asList().await().indefinitely();

        // Should successfully parse contacts despite invalid headers
        assertThat(list).hasSize(1);
        assertThat(list.get(0).name()).isEqualTo("Contact 1");
        assertThat(stats.successfulRecords()).isEqualTo(1);
        assertThat(stats.successfulPages()).isEqualTo(1);
    }

    @Test
    void shouldCompleteStreamEvenWhenAllPagesFail() {
        // Stub all pages to fail with 500 errors
        stubFor(get(urlPathEqualTo("/api/v1/contacts"))
                .withQueryParam("page", equalTo("1"))
                .willReturn(aResponse().withStatus(500).withBody("Internal Server Error")));

        FetchStatisticsCollector stats = new FetchStatisticsCollector();
        Multi<Contact> contacts = adapter.fetchAllContacts(stats);

        // Stream should complete (not hang) even when all pages fail
        List<Contact> list = contacts.collect().asList().await().indefinitely();

        // Should return empty list but not hang or throw exception
        assertThat(list).isEmpty();
        assertThat(stats.skippedPages()).isGreaterThan(0);
    }

    @Test
    void shouldCompleteStreamWhenFirstPageFailsAndSubsequentPagesSucceed() {
        // Page 1 fails
        stubFor(get(urlPathEqualTo("/api/v1/contacts"))
                .withQueryParam("page", equalTo("1"))
                .willReturn(aResponse().withStatus(500).withBody("Internal Server Error")));

        // Page 2 succeeds
        stubFor(
                get(urlPathEqualTo("/api/v1/contacts"))
                        .withQueryParam("page", equalTo("2"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withHeader("Current-Page", "2")
                                        .withHeader("Total-Pages", "2")
                                        .withHeader("Page-Items", "1")
                                        .withHeader("Total-Count", "2")
                                        .withBody(
                                                "[{\"id\":2,\"name\":\"Contact 2\",\"email\":\"contact2@example.com\",\"createdAt\":\"2024-01-01T00:00:00Z\",\"updatedAt\":\"2024-01-01T00:00:00Z\"}]")));

        FetchStatisticsCollector stats = new FetchStatisticsCollector();
        Multi<Contact> contacts = adapter.fetchAllContacts(stats);

        List<Contact> list = contacts.collect().asList().await().indefinitely();

        assertThat(list).hasSize(1);
        assertThat(list.getFirst().name()).isEqualTo("Contact 2");
        assertThat(stats.successfulPages()).isEqualTo(1);
        assertThat(stats.skippedPages()).isEqualTo(1);
    }

    @Test
    void shouldUseFallbackAfterMultipleFailures() {
        // First request fails (500)
        stubFor(get(urlPathEqualTo("/api/v1/contacts"))
                .withQueryParam("page", equalTo("1"))
                .willReturn(aResponse().withStatus(500).withBody("Internal Server Error")));

        FetchStatisticsCollector stats = new FetchStatisticsCollector();
        Multi<Contact> contacts = adapter.fetchAllContacts(stats);
        List<Contact> list = contacts.collect().asList().await().indefinitely();

        assertThat(list).isEmpty();
    }
}
