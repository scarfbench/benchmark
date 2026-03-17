package com.coffeeshop.orders.api;

import com.coffeeshop.common.domain.OrderAck;
import com.coffeeshop.common.domain.OrderRequest;
import com.coffeeshop.orders.domain.OrderEntity;
import com.coffeeshop.orders.domain.OrderRepository;
import com.coffeeshop.orders.service.OrderService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrdersResource {

    @Inject OrderService orderService;
    @Inject OrderRepository repo;

    @POST
    public Response place(OrderRequest req) {
        String id = orderService.place(req);
        return Response.accepted(new OrderAck(id)).build();
    }

    @GET @Path("/{id}")
    public OrderEntity status(@PathParam("id") long id) {
        return repo.find(id);
    }
}
