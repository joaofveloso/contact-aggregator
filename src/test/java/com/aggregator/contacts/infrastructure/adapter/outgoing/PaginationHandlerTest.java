package com.aggregator.contacts.infrastructure.adapter.outgoing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaginationHandlerTest {

    @InjectMocks
    PaginationHandler paginationHandler;

    @Test
    void shouldContinueWhenHasMorePagesAndBelowMax() {
        PaginationHandler.PaginationResult result = new PaginationHandler.PaginationResult(List.of(), 1, true);

        assertThat(paginationHandler.shouldContinue(result, 1, 100)).isTrue();
    }

    @Test
    void shouldNotContinueWhenNoMorePages() {
        PaginationHandler.PaginationResult result = new PaginationHandler.PaginationResult(List.of(), 1, false);

        assertThat(paginationHandler.shouldContinue(result, 1, 100)).isFalse();
    }

    @Test
    void shouldNotContinueWhenAtMaxPages() {
        PaginationHandler.PaginationResult result = new PaginationHandler.PaginationResult(List.of(), 100, true);

        assertThat(paginationHandler.shouldContinue(result, 100, 100)).isFalse();
    }

    @Test
    void shouldContinueWhenHasMorePagesAtBoundary() {
        PaginationHandler.PaginationResult result = new PaginationHandler.PaginationResult(List.of(), 99, true);

        assertThat(paginationHandler.shouldContinue(result, 99, 100)).isTrue();
    }
}
