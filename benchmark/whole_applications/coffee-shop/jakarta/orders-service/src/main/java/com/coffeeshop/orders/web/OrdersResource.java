package com.coffeeshop.orders.web;

import com.coffeeshop.common.domain.OrderRequest;
import com.coffeeshop.orders.service.OrderService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("/orders") // if you already have @ApplicationPath("/api"), change this to just "/orders"
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OrdersResource {

  @Inject OrderService orderService;

  @POST
  public Response create(@Valid OrderRequest request) {
    String id = orderService.place(request); // <-- calls your service
    // use 202 if you treat it as async, 201 if you consider it created now
    return Response.accepted(Map.of("id", id)).build();
  }

  @GET
  @Path("/{id}")
  public Response get(@PathParam("id") long id) {
    // if you already have a repository method, reuse it here
    // shown minimal for completeness
    return Response.ok(Map.of("id", id)).build();
  }
}
