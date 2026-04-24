package org.springframework.samples.petclinic.api;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@RequestScoped
@Path("/gateway")
public class GatewayResource {

    @Inject
    GatewayService service;

    @GET
    @Path("/owners/{ownerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOwnerDetails(@PathParam("ownerId") int ownerId) {
        JsonObject result = service.getOwnerDetails(ownerId);
        if (result == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(result).build();
    }
}
