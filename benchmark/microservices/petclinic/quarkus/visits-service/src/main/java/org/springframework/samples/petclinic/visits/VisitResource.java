package org.springframework.samples.petclinic.visits;

import jakarta.transaction.Transactional;
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
import java.util.List;

@Path("/")
public class VisitResource {

    @POST
    @Path("/owners/{ownerId}/pets/{petId}/visits")
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response create(@PathParam("ownerId") int ownerId,
                           @PathParam("petId") int petId,
                           Visit visit) {
        visit.id = null;
        visit.petId = petId;
        visit.persist();
        return Response.status(Response.Status.CREATED).entity(visit).build();
    }

    @GET
    @Path("/owners/{ownerId}/pets/{petId}/visits")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Visit> findByPetId(@PathParam("ownerId") int ownerId,
                                    @PathParam("petId") int petId) {
        return Visit.list("petId", petId);
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
        List<Visit> items;
        if (ids.isEmpty()) {
            items = new ArrayList<>();
        } else {
            items = Visit.list("petId in ?1", ids);
        }
        return new Visits(items);
    }

    public static class Visits {
        public List<Visit> items;
        public Visits() { this.items = new ArrayList<>(); }
        public Visits(List<Visit> items) { this.items = items; }
    }
}
