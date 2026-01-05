package com.aggregator.contacts.infrastructure.adapter.outgoing.dto;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class KenectLabsContactDtoTest {

    @Test
    void shouldCreateDtoWithConstructor() {
        KenectLabsContactDto dto = new KenectLabsContactDto(
                1L, "John Doe", "john@example.com", "2024-01-01T00:00:00Z", "2024-01-01T00:00:00Z");

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("John Doe");
        assertThat(dto.getEmail()).isEqualTo("john@example.com");
        assertThat(dto.getCreatedAt()).isEqualTo("2024-01-01T00:00:00Z");
        assertThat(dto.getUpdatedAt()).isEqualTo("2024-01-01T00:00:00Z");
    }

    @Test
    void shouldCreateDtoWithNullValues() {
        KenectLabsContactDto dto = new KenectLabsContactDto(null, null, null, null, null);

        assertThat(dto.getId()).isNull();
        assertThat(dto.getName()).isNull();
        assertThat(dto.getEmail()).isNull();
        assertThat(dto.getCreatedAt()).isNull();
        assertThat(dto.getUpdatedAt()).isNull();
    }

    @Test
    void shouldHaveEqualsBasedOnId() {
        KenectLabsContactDto dto1 =
                new KenectLabsContactDto(1L, "John", "john@example.com", "2024-01-01", "2024-01-01");
        KenectLabsContactDto dto2 =
                new KenectLabsContactDto(1L, "Jane", "jane@example.com", "2024-01-02", "2024-01-02");
        KenectLabsContactDto dto3 =
                new KenectLabsContactDto(2L, "John", "john@example.com", "2024-01-01", "2024-01-01");

        assertThat(dto1).isEqualTo(dto2).hasSameHashCodeAs(dto2).isNotEqualTo(dto3);
    }

    @Test
    void shouldBeEqualToItself() {
        KenectLabsContactDto dto = new KenectLabsContactDto(1L, "John", "john@example.com", "2024-01-01", "2024-01-01");
        assertThat(dto).isEqualTo(dto);
    }

    @Test
    void shouldNotBeEqualToNull() {
        KenectLabsContactDto dto = new KenectLabsContactDto(1L, "John", "john@example.com", "2024-01-01", "2024-01-01");
        assertThat(dto).isNotEqualTo(null);
    }

    @Test
    void shouldNotBeEqualToDifferentClass() {
        KenectLabsContactDto dto = new KenectLabsContactDto(1L, "John", "john@example.com", "2024-01-01", "2024-01-01");
        assertThat(dto).isNotEqualTo("not a dto");
    }

    @Test
    void shouldNotBeEqualToWhenIdsDiffer() {
        KenectLabsContactDto dto1 =
                new KenectLabsContactDto(1L, "John", "john@example.com", "2024-01-01", "2024-01-01");
        KenectLabsContactDto dto2 =
                new KenectLabsContactDto(2L, "John", "john@example.com", "2024-01-01", "2024-01-01");
        assertThat(dto1).isNotEqualTo(dto2);
    }

    @Test
    void shouldBeEqualToWhenBothIdsAreNull() {
        KenectLabsContactDto dto1 =
                new KenectLabsContactDto(null, "John", "john@example.com", "2024-01-01", "2024-01-01");
        KenectLabsContactDto dto2 =
                new KenectLabsContactDto(null, "Jane", "jane@example.com", "2024-01-02", "2024-01-02");

        // When both IDs are null, Objects.equals returns true
        assertThat(dto1).isEqualTo(dto2);
    }
}
