package com.aggregator.contacts.infrastructure.adapter.incoming;

import com.aggregator.contacts.application.ContactApplicationService;
import com.aggregator.contacts.application.FetchStatisticsDTO;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

@Tag(name = "Contacts", description = "Contact aggregation and retrieval operations")
@Path("/v1/contacts")
@Produces(MediaType.APPLICATION_JSON)
public class ContactResource {

    private static final Logger LOG = Logger.getLogger(ContactResource.class);
    private final ContactApplicationService service;

    public ContactResource(ContactApplicationService service) {
        this.service = service;
    }

    public record CacheInvalidationResponse(String message) {}

    @GET
    @Operation(
            summary = "Retrieve all contacts",
            description =
                    "Fetches and aggregates all contacts from Kenect Labs API with automatic pagination handling.")
    @APIResponse(
            responseCode = "200",
            description = "Successfully retrieved contacts",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(type = SchemaType.ARRAY, implementation = FetchStatisticsDTO.class)))
    @APIResponse(responseCode = "500", description = "External API error")
    @APIResponse(responseCode = "503", description = "Service unavailable")
    public Uni<Response> getAllContacts() {
        LOG.info("Received request to fetch all contacts");
        return service.fetchAllContactsReactive().map(dto -> Response.ok(dto).build());
    }

    @POST
    @Path("/invalidate-cache")
    @Operation(
            summary = "Invalidate contact cache",
            description =
                    "Clears the contacts cache, forcing the next fetch to retrieve fresh data from the external API.")
    @APIResponse(
            responseCode = "200",
            description = "Cache invalidated successfully",
            content =
                    @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CacheInvalidationResponse.class)))
    public Response invalidateCache() {
        service.invalidateCache();
        return Response.ok(new CacheInvalidationResponse("Cache invalidated successfully")).build();
    }
}
