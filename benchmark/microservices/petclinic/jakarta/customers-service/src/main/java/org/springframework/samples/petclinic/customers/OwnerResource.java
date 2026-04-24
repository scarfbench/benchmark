package org.springframework.samples.petclinic.customers;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Optional;

@RequestScoped
@Path("/owners")
public class OwnerResource {

    @Inject
    CustomersStore store;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Owner> findAll() {
        return store.findAllOwners();
    }

    @GET
    @Path("/{ownerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findOwner(@PathParam("ownerId") int ownerId) {
        Optional<Owner> o = store.findOwner(ownerId);
        if (o.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(o.get()).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createOwner(Owner ownerRequest) {
        Owner created = store.createOwner(ownerRequest);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }
}
