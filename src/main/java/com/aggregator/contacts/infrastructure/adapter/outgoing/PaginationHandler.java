package com.aggregator.contacts.infrastructure.adapter.outgoing;

import com.aggregator.contacts.domain.model.Contact;
import com.aggregator.contacts.domain.model.FetchStatisticsCollector;
import com.aggregator.contacts.infrastructure.adapter.outgoing.dto.KenectLabsContactDto;
import com.aggregator.contacts.infrastructure.adapter.outgoing.mapper.ContactMapper;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.buffer.Buffer;
import io.vertx.mutiny.ext.web.client.HttpResponse;
import io.vertx.mutiny.ext.web.client.WebClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import org.jboss.logging.Logger;

@ApplicationScoped
public class PaginationHandler {

    private static final Logger LOG = Logger.getLogger(PaginationHandler.class);
    private static final ContactMapper MAPPER = ContactMapper.INSTANCE;

    public boolean shouldContinue(PaginationResult result, int currentPage, int maxPages) {
        return result.hasMorePages() && currentPage < maxPages;
    }

    public Uni<PaginationResult> fetchPage(PageRequest request) {
        LOG.debugf("Fetching page %d from %s", request.page(), request.url());

        return executeRequest(request)
                .onItem()
                .transformToUni(response -> {
                    LOG.tracef("Received response %d for page %d", response.statusCode(), request.page());
                    return handleResponse(response, request);
                })
                .onFailure(IOException.class)
                .retry()
                .atMost(request.retryAttempts())
                .onFailure()
                .recoverWithItem(e -> handleFailure(e, request));
    }

    private Uni<HttpResponse<Buffer>> executeRequest(PageRequest request) {
        LOG.tracef("Executing HTTP GET request: %s", request.url());
        return request.webClient()
                .getAbs(request.url())
                .putHeader(HttpHeaders.AUTHORIZATION, "Bearer " + request.token())
                .putHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                .send();
    }

    private Uni<PaginationResult> handleResponse(HttpResponse<Buffer> response, PageRequest request) {
        return switch (response.statusCode()) {
            case 200 -> Uni.createFrom().item(parseResponse(response, request));
            case 503, 504 ->
                Uni.createFrom().failure(new IOException("Transient HTTP error: " + response.statusCode()));
            default -> {
                LOG.warnf(
                        "HTTP %d fetching page %d - check API availability and credentials",
                        response.statusCode(), request.page());
                request.statistics().incrementSkippedPages();
                yield Uni.createFrom().item(emptyResultContinue(request.page(), request.maxPages()));
            }
        };
    }

    private PaginationResult handleFailure(Throwable e, PageRequest request) {
        String rootCause = getRootCauseMessage(e);

        if (isConnectionError(e)) {
            LOG.warnf(
                    "Cannot connect to API for page %d after %d attempts: %s",
                    request.page(), request.retryAttempts(), rootCause);
            LOG.infof("Check if API is running at: %s", request.url());
        } else {
            LOG.warnf(
                    "Failed to fetch page %d after %d attempts: %s",
                    request.page(), request.retryAttempts(), rootCause);
        }

        LOG.tracef("Full exception for page %d:", request.page(), e);
        request.statistics().incrementSkippedPages();
        return emptyResult(request.page(), request.maxPages());
    }

    private String getRootCauseMessage(Throwable e) {
        Throwable cause = e;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        String message = cause.getMessage();
        return message != null ? message : cause.getClass().getSimpleName();
    }

    private boolean isConnectionError(Throwable e) {
        String message = e.getMessage();
        if (message == null) {
            return false;
        }
        return message.contains("Connection refused")
                || message.contains("No route to host")
                || message.contains("Network is unreachable")
                || message.contains("Timeout");
    }

    private PaginationResult parseResponse(HttpResponse<Buffer> response, PageRequest request) {
        JsonArray jsonArray = new JsonArray(response.bodyAsString());
        List<Contact> contacts = jsonArray.stream()
                .map(this::jsonToDto)
                .map(dto -> mapContactOrLogFailure(dto, request.statistics(), request.source()))
                .filter(Objects::nonNull)
                .toList();

        request.statistics().incrementSuccessfulRecords(contacts.size());
        request.statistics().incrementSuccessfulPages();

        boolean hasMorePages = parseHasMorePages(response, jsonArray.size(), request.pageSize());
        return new PaginationResult(contacts, request.page(), hasMorePages);
    }

    private KenectLabsContactDto jsonToDto(Object obj) {
        JsonObject json = (JsonObject) obj;
        return new KenectLabsContactDto(
                json.getLong("id"),
                json.getString("name"),
                json.getString("email"),
                json.getString("createdAt"),
                json.getString("updatedAt"));
    }

    private Contact mapContactOrLogFailure(
            KenectLabsContactDto dto, FetchStatisticsCollector statistics, String source) {
        try {
            return MAPPER.toDomain(dto, source);
        } catch (Exception e) {
            LOG.warnf("Failed to map contact ID %s: %s", dto.getId(), e.getMessage());
            statistics.incrementSkippedRecords();
            return null;
        }
    }

    private boolean parseHasMorePages(HttpResponse<?> response, int itemCount, int pageSize) {
        String currentPage = response.getHeader("Current-Page");
        String totalPages = response.getHeader("Total-Pages");

        if (currentPage != null && totalPages != null) {
            try {
                return Integer.parseInt(currentPage) < Integer.parseInt(totalPages);
            } catch (NumberFormatException e) {
                LOG.warn("Invalid pagination headers, using fallback");
            }
        }
        return itemCount >= pageSize;
    }

    private PaginationResult emptyResult(int page, int maxPages) {
        // When a page fails, we stop pagination instead of continuing
        // This prevents infinite pagination when all pages fail or circuit breaker opens
        return new PaginationResult(List.of(), page, false);
    }

    private PaginationResult emptyResultContinue(int page, int maxPages) {
        // Use this when we want to skip a failed page and try the next one
        return new PaginationResult(List.of(), page, page < maxPages);
    }

    public record PaginationResult(List<Contact> contacts, int currentPage, boolean hasMorePages) {}

    public record PageRequest(
            WebClient webClient,
            String url,
            String token,
            int page,
            FetchStatisticsCollector statistics,
            int retryAttempts,
            int pageSize,
            int maxPages,
            String source) {
        public PageRequest {
            Objects.requireNonNull(webClient);
            Objects.requireNonNull(url);
            Objects.requireNonNull(statistics);
            Objects.requireNonNull(source);
        }
    }
}
