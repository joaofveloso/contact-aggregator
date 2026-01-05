package com.aggregator.contacts.infrastructure.adapter.outgoing.mapper;

import static org.assertj.core.api.Assertions.*;

import com.aggregator.contacts.domain.model.Contact;
import com.aggregator.contacts.infrastructure.adapter.outgoing.dto.KenectLabsContactDto;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ContactMapperTest {

    private static final String SOURCE = "KENECT_LABS";

    @Test
    void shouldMapDtoToDomain() {
        KenectLabsContactDto dto = new KenectLabsContactDto();
        dto.setId(1L);
        dto.setName("John Doe");
        dto.setEmail("john@example.com");
        dto.setCreatedAt("2024-01-01T12:00:00Z");
        dto.setUpdatedAt("2024-01-01T12:00:00Z");

        Contact contact = ContactMapper.INSTANCE.toDomain(dto, SOURCE);

        assertThat(contact.id()).isEqualTo(1L);
        assertThat(contact.name()).isEqualTo("John Doe");
        assertThat(contact.email()).isEqualTo("john@example.com");
        assertThat(contact.source()).isEqualTo(SOURCE);
        assertThat(contact.createdAt()).isEqualTo(Instant.parse("2024-01-01T12:00:00Z"));
        assertThat(contact.updatedAt()).isEqualTo(Instant.parse("2024-01-01T12:00:00Z"));
    }

    @Test
    void shouldHandleNullTimestamps() {
        KenectLabsContactDto dto = new KenectLabsContactDto();
        dto.setId(1L);
        dto.setName("John Doe");
        dto.setEmail("john@example.com");
        dto.setCreatedAt(null);
        dto.setUpdatedAt(null);

        assertThatThrownBy(() -> ContactMapper.INSTANCE.toDomain(dto, SOURCE)).isInstanceOf(NullPointerException.class);
    }
}
