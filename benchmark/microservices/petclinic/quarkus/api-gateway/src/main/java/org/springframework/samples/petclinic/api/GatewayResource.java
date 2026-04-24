package org.springframework.samples.petclinic.api;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/gateway")
public class GatewayResource {

    @Inject
    GatewayService service;

    @GET
    @Path("/owners/{ownerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOwnerDetails(@PathParam("ownerId") int ownerId) {
        OwnerDetails details = service.getOwnerDetails(ownerId);
        if (details == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(details).build();
    }
}
