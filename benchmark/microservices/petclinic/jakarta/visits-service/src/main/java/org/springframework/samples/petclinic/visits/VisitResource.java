package org.springframework.samples.petclinic.visits;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequestScoped
@Path("/")
public class VisitResource {

    @Inject
    VisitsStore store;

    @POST
    @Path("/owners/{ownerId}/pets/{petId}/visits")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@PathParam("ownerId") int ownerId,
                           @PathParam("petId") int petId,
                           Visit visit) {
        Visit created = store.create(petId, visit);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @GET
    @Path("/owners/{ownerId}/pets/{petId}/visits")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Visit> findByPetId(@PathParam("ownerId") int ownerId,
                                    @PathParam("petId") int petId) {
        return store.findByPetId(petId);
    }

    @GET
    @Path("/pets/visits")
    @Produces(MediaType.APPLICATION_JSON)
    public Visits findByPetIds(@QueryParam("petId") String petIdsParam) {
        List<Integer> ids = new ArrayList<>();
        if (petIdsParam != null && !petIdsParam.isEmpty()) {
            for (String s : petIdsParam.split(",")) {
                try {
                    ids.add(Integer.parseInt(s.trim()));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return new Visits(store.findByPetIdIn(ids));
    }

    public static class Visits {
        public List<Visit> items;
        public Visits() { this.items = new ArrayList<>(); }
        public Visits(List<Visit> items) { this.items = items; }
        public List<Visit> getItems() { return items; }
        public void setItems(List<Visit> items) { this.items = items; }
    }
}
