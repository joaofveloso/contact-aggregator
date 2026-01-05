package com.aggregator.contacts.infrastructure.config;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class KenectLabsApiConfig {

    @ConfigProperty(
            name = "kenect.labs.api.base-url",
            defaultValue = "https://candidate-challenge-api-489237493095.us-central1.run.app")
    private String baseUrl;

    @ConfigProperty(name = "kenect.labs.api.bearer-token", defaultValue = "test-token")
    private String bearerToken;

    @ConfigProperty(name = "kenect.labs.api.contacts-path", defaultValue = "/api/v1/contacts")
    private String contactsPath;

    @ConfigProperty(name = "kenect.labs.api.source", defaultValue = "KENECT_LABS")
    private String source;

    @ConfigProperty(name = "kenect.labs.api.timeout-seconds", defaultValue = "30")
    private int timeoutSeconds;

    @ConfigProperty(name = "kenect.labs.api.retry-attempts", defaultValue = "1")
    private int retryAttempts;

    @ConfigProperty(name = "kenect.labs.api.max-pages", defaultValue = "100")
    private int maxPages;

    @ConfigProperty(name = "kenect.labs.api.page-size", defaultValue = "20")
    private int pageSize;

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getBearerToken() {
        return bearerToken;
    }

    public String getContactsPath() {
        return contactsPath;
    }

    public String getSource() {
        return source;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }

    public int getMaxPages() {
        return maxPages;
    }

    public int getPageSize() {
        return pageSize;
    }
}
