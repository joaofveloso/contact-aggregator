package com.aggregator.contacts.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.aggregator.contacts.domain.model.Contact;
import com.aggregator.contacts.domain.model.FetchStatisticsCollector;
import com.aggregator.contacts.domain.port.ContactRepository;
import io.smallrye.mutiny.Multi;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContactApplicationServiceTest {

    @Mock
    ContactRepository repository;

    @InjectMocks
    ContactApplicationService service;

    @Test
    void shouldFetchAllContactsReactive() {
        // Given
        Contact contact = new Contact(1L, "John Doe", "john@example.com", "KENECT_LABS", Instant.now(), Instant.now());

        // Use ArgumentCaptor to capture the FetchStatisticsCollector
        ArgumentCaptor<FetchStatisticsCollector> captor = ArgumentCaptor.forClass(FetchStatisticsCollector.class);
        when(repository.fetchAllContacts(captor.capture())).thenAnswer(invocation -> {
            // Simulate statistics collection
            FetchStatisticsCollector stats = invocation.getArgument(0);
            stats.incrementSuccessfulRecords(1);
            stats.incrementSuccessfulPages();
            return Multi.createFrom().item(contact);
        });

        // When
        FetchStatisticsDTO result = service.fetchAllContactsReactive().await().indefinitely();

        // Then
        assertThat(result.successfulRecords()).isEqualTo(1);
        assertThat(result.contacts()).hasSize(1);
        assertThat(result.contacts().getFirst().name()).isEqualTo("John Doe");
        verify(repository).fetchAllContacts(any());
    }

    @Test
    void shouldReturnEmptyListWhenNoContacts() {
        // Given
        ArgumentCaptor<FetchStatisticsCollector> captor = ArgumentCaptor.forClass(FetchStatisticsCollector.class);
        when(repository.fetchAllContacts(captor.capture()))
                .thenAnswer(invocation -> Multi.createFrom().empty());

        // When
        FetchStatisticsDTO result = service.fetchAllContactsReactive().await().indefinitely();

        // Then
        assertThat(result.successfulRecords()).isZero();
        assertThat(result.contacts()).isEmpty();
    }

    @Test
    void shouldInvalidateCache() {
        // When/Then - should not throw
        assertThatCode(() -> service.invalidateCache()).doesNotThrowAnyException();
    }
}
