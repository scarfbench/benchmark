package org.springframework.samples.petclinic.vets;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/vets")
public class VetResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Vet> findAll() {
        return Vet.listAll();
    }
}
