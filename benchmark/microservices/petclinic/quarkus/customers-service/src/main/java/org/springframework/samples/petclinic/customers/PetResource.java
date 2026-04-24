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

@Path("/")
public class PetResource {

    @GET
    @Path("/petTypes")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PetType> getPetTypes() {
        return PetType.listAll();
    }

    @GET
    @Path("/owners/{ownerId}/pets/{petId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findPet(@PathParam("ownerId") int ownerId, @PathParam("petId") int petId) {
        Pet p = Pet.findById(petId);
        if (p == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(p).build();
    }

    @POST
    @Path("/owners/{ownerId}/pets")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createPet(@PathParam("ownerId") int ownerId, Pet newPet) {
        Owner owner = Owner.findById(ownerId);
        if (owner == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Owner " + ownerId + " not found").build();
        }
        newPet.id = null;
        newPet.owner = owner;
        if (newPet.type != null && newPet.type.id != null) {
            PetType t = PetType.findById(newPet.type.id);
            if (t != null) newPet.type = t;
        }
        newPet.persist();
        return Response.status(Response.Status.CREATED).entity(newPet).build();
    }
}
