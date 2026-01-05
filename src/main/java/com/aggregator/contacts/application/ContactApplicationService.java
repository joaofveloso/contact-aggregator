package com.aggregator.contacts.application;

import com.aggregator.contacts.domain.model.FetchStatisticsCollector;
import com.aggregator.contacts.domain.port.ContactRepository;
import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheResult;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ContactApplicationService {

    private static final Logger LOG = Logger.getLogger(ContactApplicationService.class);

    private final ContactRepository repository;

    public ContactApplicationService(ContactRepository repository) {
        this.repository = repository;
    }

    @CacheResult(cacheName = "contacts-cache")
    public Uni<FetchStatisticsDTO> fetchAllContactsReactive() {
        LOG.info("Cache miss - fetching contacts from external API");
        FetchStatisticsCollector statistics = new FetchStatisticsCollector();

        return repository
                .fetchAllContacts(statistics)
                .collect()
                .asList()
                .map(contacts -> FetchStatisticsDTO.from(statistics, contacts));
    }

    @CacheInvalidate(cacheName = "contacts-cache")
    public void invalidateCache() {
        LOG.info("Cache invalidated");
    }
}
