package com.ibm.sample.daytrader.web;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {

    @GET
    @Path("/health")
    public Response health() {
        return Response.ok("{\"status\":\"UP\"}").build();
    }

    @GET
    @Path("/")
    public Response root() {
        return Response.ok("<html><body><h1>DayTrader Web Service (Jakarta EE)</h1><p>Status: Running</p></body></html>")
            .type(MediaType.TEXT_HTML).build();
    }

    @GET
    @Path("/servlet/PingServlet")
    public Response ping() {
        return Response.ok("<html><body><h1>Ping!</h1><p>DayTrader Web Service is alive.</p></body></html>")
            .type(MediaType.TEXT_HTML).build();
    }
}
