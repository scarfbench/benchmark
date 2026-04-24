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
@Path("/")
public class PetResource {

    @Inject
    CustomersStore store;

    @GET
    @Path("/petTypes")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PetType> getPetTypes() {
        return store.findPetTypes();
    }

    @POST
    @Path("/owners/{ownerId}/pets")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createPet(@PathParam("ownerId") int ownerId, Pet pet) {
        if (store.findOwner(ownerId).isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).entity("Owner " + ownerId + " not found").build();
        }
        if (pet.type != null && pet.type.id != null) {
            store.findPetTypeById(pet.type.id).ifPresent(t -> pet.type = t);
        }
        Pet created = store.createPet(ownerId, pet);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @GET
    @Path("/owners/{ownerId}/pets/{petId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response findPet(@PathParam("ownerId") int ownerId, @PathParam("petId") int petId) {
        Optional<Pet> p = store.findPet(petId);
        if (p.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(p.get()).build();
    }
}
