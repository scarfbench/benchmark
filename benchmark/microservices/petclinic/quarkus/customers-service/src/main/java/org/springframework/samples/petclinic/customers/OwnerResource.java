package org.springframework.samples.petclinic.customers;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/owners")
public class OwnerResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Owner> findAll() {
        return Owner.listAll();
    }

    @GET
    @Path("/{ownerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findOwner(@PathParam("ownerId") int ownerId) {
        Owner o = Owner.findById(ownerId);
        if (o == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(o).build();
    }

    @POST
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createOwner(Owner newOwner) {
        newOwner.id = null;
        newOwner.persist();
        return Response.status(Response.Status.CREATED).entity(newOwner).build();
    }
}
