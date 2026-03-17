package quarkus.tutorial.web.servlet;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/greeting")
public class Greeting {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response greet(@QueryParam("name") String name) {
        if (name == null || name.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error: 'name' parameter is required").build();
        }

        String greeting = "Hello, " + name + "!";
        return Response.ok(greeting).build();
    }
}
