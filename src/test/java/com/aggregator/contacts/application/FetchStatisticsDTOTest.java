package com.aggregator.contacts.application;

import static org.assertj.core.api.Assertions.*;

import com.aggregator.contacts.domain.model.Contact;
import com.aggregator.contacts.domain.model.FetchStatisticsCollector;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class FetchStatisticsDTOTest {

    @Test
    void shouldCreateFromCollectorAndContacts() {
        // Given
        FetchStatisticsCollector collector = new FetchStatisticsCollector();
        collector.incrementSuccessfulRecords(10);
        collector.incrementSkippedRecords();
        collector.incrementSuccessfulPages();
        collector.incrementSkippedPages();

        Contact contact = new Contact(1L, "John Doe", "john@example.com", "KENECT_LABS", Instant.now(), Instant.now());
        List<Contact> contacts = List.of(contact);

        // When
        FetchStatisticsDTO dto = FetchStatisticsDTO.from(collector, contacts);

        // Then
        assertThat(dto.successfulRecords()).isEqualTo(10);
        assertThat(dto.skippedRecords()).isEqualTo(1);
        assertThat(dto.successfulPages()).isEqualTo(1);
        assertThat(dto.skippedPages()).isEqualTo(1);
        assertThat(dto.circuitBreakerTrippedCount()).isZero();
        assertThat(dto.contacts()).isEqualTo(contacts);
    }

    @Test
    void shouldCreateDefaultInstance() {
        // When
        FetchStatisticsDTO dto = new FetchStatisticsDTO();

        // Then
        assertThat(dto.successfulRecords()).isZero();
        assertThat(dto.skippedRecords()).isZero();
        assertThat(dto.successfulPages()).isZero();
        assertThat(dto.skippedPages()).isZero();
        assertThat(dto.circuitBreakerTrippedCount()).isZero();
        assertThat(dto.contacts()).isEmpty();
    }
}
