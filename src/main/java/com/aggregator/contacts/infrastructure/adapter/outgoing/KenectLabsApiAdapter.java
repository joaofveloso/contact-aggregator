package com.aggregator.contacts.infrastructure.adapter.outgoing;

import com.aggregator.contacts.domain.model.Contact;
import com.aggregator.contacts.domain.model.FetchStatisticsCollector;
import com.aggregator.contacts.domain.port.ContactRepository;
import com.aggregator.contacts.infrastructure.config.KenectLabsApiConfig;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class KenectLabsApiAdapter implements ContactRepository {

    private static final Logger LOG = LoggerFactory.getLogger(KenectLabsApiAdapter.class);

    private final Vertx vertx;
    private final PaginationHandler paginationHandler;
    private final KenectLabsApiConfig config;
    private WebClient webClient;

    public KenectLabsApiAdapter(Vertx vertx, PaginationHandler paginationHandler, KenectLabsApiConfig config) {
        this.vertx = vertx;
        this.paginationHandler = paginationHandler;
        this.config = config;
    }

    @PostConstruct
    void init() {
        // Enable SSL only for HTTPS URLs
        boolean useSsl = config.getBaseUrl().startsWith("https://");
        this.webClient = WebClient.create(
                vertx,
                new WebClientOptions()
                        .setConnectTimeout((int)
                                Duration.ofSeconds(config.getTimeoutSeconds()).toMillis())
                        .setSsl(useSsl));
    }

    @Override
    public Multi<Contact> fetchAllContacts(FetchStatisticsCollector statistics) {
        return fetchPageWithContinuation(1, statistics);
    }

    private Multi<Contact> fetchPageWithContinuation(int page, FetchStatisticsCollector statistics) {
        return fetchPage(page, statistics).onItem().transformToMulti(result -> {
            Multi<Contact> currentContacts =
                    Multi.createFrom().items(result.contacts().toArray(new Contact[0]));
            return paginationHandler.shouldContinue(result, page, config.getMaxPages())
                    ? Multi.createBy()
                            .concatenating()
                            .streams(currentContacts, fetchPageWithContinuation(page + 1, statistics))
                    : currentContacts;
        });
    }

    @CircuitBreaker(requestVolumeThreshold = 4, delay = 10000, successThreshold = 2)
    @Timeout(value = 30, unit = ChronoUnit.SECONDS)
    @Fallback(fallbackMethod = "fetchPageFallback")
    Uni<PaginationHandler.PaginationResult> fetchPage(int page, FetchStatisticsCollector statistics) {
        String fullUrl = config.getBaseUrl() + config.getContactsPath();
        PaginationHandler.PageRequest request = new PaginationHandler.PageRequest(
                webClient,
                fullUrl + "?page=" + page,
                config.getBearerToken(),
                page,
                statistics,
                config.getRetryAttempts(),
                config.getPageSize(),
                config.getMaxPages(),
                config.getSource());
        return paginationHandler.fetchPage(request);
    }

    Uni<PaginationHandler.PaginationResult> fetchPageFallback(int page, FetchStatisticsCollector statistics) {

        LOG.warn("Circuit breaker OPEN or timeout on page {} - returning empty page", page);
        statistics.incrementCircuitBreakerTripped();

        return Uni.createFrom().item(new PaginationHandler.PaginationResult(List.of(), page, false));
    }
}
