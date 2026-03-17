package com.coffeeshop.barista.api;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/status")
public class StatusResource {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String ok() {
        return "barista ok";
    }
}
