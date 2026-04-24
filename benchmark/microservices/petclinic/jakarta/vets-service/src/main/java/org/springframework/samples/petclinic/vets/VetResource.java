package org.springframework.samples.petclinic.vets;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@RequestScoped
@Path("/vets")
public class VetResource {

    @Inject
    VetsStore store;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Vet> findAll() {
        return store.findAll();
    }
}
