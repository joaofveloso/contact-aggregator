package com.aggregator.contacts.infrastructure.adapter.outgoing.mapper;

import com.aggregator.contacts.domain.model.Contact;
import com.aggregator.contacts.infrastructure.adapter.outgoing.dto.KenectLabsContactDto;
import java.time.Instant;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ContactMapper {

    ContactMapper INSTANCE = Mappers.getMapper(ContactMapper.class);

    default Contact toDomain(KenectLabsContactDto dto, String source) {
        return new Contact(
                dto.getId(), dto.getName(), dto.getEmail(), source, map(dto.getCreatedAt()), map(dto.getUpdatedAt()));
    }

    default Instant map(String value) {
        if (value == null) {
            throw new NullPointerException("Timestamp cannot be null");
        }
        return Instant.parse(value);
    }
}
