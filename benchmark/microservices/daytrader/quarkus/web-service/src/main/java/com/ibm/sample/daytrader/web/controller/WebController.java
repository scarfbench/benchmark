package com.ibm.sample.daytrader.web.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
@ApplicationScoped
public class WebController {

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response index() {
        String html = "<!DOCTYPE html>\n" +
            "<html><head><title>DayTrader - Quarkus</title></head>\n" +
            "<body>\n" +
            "<h1>DayTrader Web Interface (Quarkus)</h1>\n" +
            "<p>Welcome to the DayTrader Microservices Application running on Quarkus.</p>\n" +
            "<ul>\n" +
            "  <li><a href=\"/q/health\">Health Check</a></li>\n" +
            "  <li><a href=\"/servlet/PingServlet\">Ping Servlet</a></li>\n" +
            "</ul>\n" +
            "</body></html>";
        return Response.ok(html).build();
    }

    @GET
    @Path("/servlet/PingServlet")
    @Produces(MediaType.TEXT_HTML)
    public Response pingServlet() {
        String html = "<!DOCTYPE html>\n" +
            "<html><head><title>Ping</title></head>\n" +
            "<body>\n" +
            "<h2>Ping Servlet</h2>\n" +
            "<p>Ping! DayTrader Web Service is alive and running on Quarkus.</p>\n" +
            "<p>Timestamp: " + new java.util.Date() + "</p>\n" +
            "</body></html>";
        return Response.ok(html).build();
    }
}
