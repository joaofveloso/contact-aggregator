package com.aggregator.contacts.domain.port;

import com.aggregator.contacts.domain.model.Contact;
import com.aggregator.contacts.domain.model.FetchStatisticsCollector;
import io.smallrye.mutiny.Multi;

public interface ContactRepository {

    Multi<Contact> fetchAllContacts(FetchStatisticsCollector statistics);
}
