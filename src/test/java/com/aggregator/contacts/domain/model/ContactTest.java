package com.aggregator.contacts.domain.model;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class ContactTest {

    @Test
    void shouldCreateContactWithAllFields() {
        Instant now = Instant.now();
        Contact contact = new Contact(1L, "John Doe", "john@example.com", "KENECT_LABS", now, now);

        assertThat(contact.id()).isEqualTo(1L);
        assertThat(contact.name()).isEqualTo("John Doe");
        assertThat(contact.email()).isEqualTo("john@example.com");
        assertThat(contact.source()).isEqualTo("KENECT_LABS");
        assertThat(contact.createdAt()).isEqualTo(now);
        assertThat(contact.updatedAt()).isEqualTo(now);
    }

    @Test
    void shouldHaveEqualsAndHashCodeBasedOnId() {
        Contact contact1 = new Contact(1L, "John", "john@example.com", "KENECT_LABS", Instant.now(), Instant.now());
        Contact contact2 = new Contact(1L, "Jane", "jane@example.com", "KENECT_LABS", Instant.now(), Instant.now());
        Contact contact3 = new Contact(2L, "John", "john@example.com", "KENECT_LABS", Instant.now(), Instant.now());

        assertThat(contact1).isEqualTo(contact2);
        assertThat(contact1.hashCode()).isEqualTo(contact2.hashCode());
        assertThat(contact1).isNotEqualTo(contact3);
    }

    @Test
    void shouldThrowWhenIdIsNull() {
        Instant now = Instant.now();
        assertThatThrownBy(() -> new Contact(null, "John", "john@example.com", "KENECT_LABS", now, now))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("id");
    }

    @Test
    void shouldThrowWhenNameIsNull() {
        Instant now = Instant.now();
        assertThatThrownBy(() -> new Contact(1L, null, "john@example.com", "KENECT_LABS", now, now))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("name");
    }

    @Test
    void shouldThrowWhenEmailIsNull() {
        Instant now = Instant.now();
        assertThatThrownBy(() -> new Contact(1L, "John", null, "KENECT_LABS", now, now))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("email");
    }

    @Test
    void shouldThrowWhenSourceIsNull() {
        Instant now = Instant.now();
        assertThatThrownBy(() -> new Contact(1L, "John", "john@example.com", null, now, now))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("source");
    }

    @Test
    void shouldThrowWhenCreatedAtIsNull() {
        assertThatThrownBy(() -> new Contact(1L, "John", "john@example.com", "KENECT_LABS", null, Instant.now()))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("createdAt");
    }

    @Test
    void shouldThrowWhenUpdatedAtIsNull() {
        assertThatThrownBy(() -> new Contact(1L, "John", "john@example.com", "KENECT_LABS", Instant.now(), null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("updatedAt");
    }

    @Test
    void shouldBeEqualToItself() {
        Contact contact = new Contact(1L, "John", "john@example.com", "KENECT_LABS", Instant.now(), Instant.now());
        assertThat(contact).isEqualTo(contact);
    }

    @Test
    void shouldNotBeEqualToNull() {
        Contact contact = new Contact(1L, "John", "john@example.com", "KENECT_LABS", Instant.now(), Instant.now());
        assertThat(contact).isNotEqualTo(null);
    }

    @Test
    void shouldNotBeEqualToDifferentClass() {
        Contact contact = new Contact(1L, "John", "john@example.com", "KENECT_LABS", Instant.now(), Instant.now());
        assertThat(contact).isNotEqualTo("not a contact");
    }

    @Test
    void shouldNotBeEqualToWhenIdsDiffer() {
        Contact contact1 = new Contact(1L, "John", "john@example.com", "KENECT_LABS", Instant.now(), Instant.now());
        Contact contact2 = new Contact(2L, "John", "john@example.com", "KENECT_LABS", Instant.now(), Instant.now());
        assertThat(contact1).isNotEqualTo(contact2);
    }
}
