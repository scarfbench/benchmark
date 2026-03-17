package quarkus.tutorial.cart;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import quarkus.tutorial.cart.service.Cart;
import quarkus.tutorial.cart.util.BookException;

/**
 * REST resource for cart operations.
 * 
 * Provides HTTP endpoints to interact with the shopping cart EJB.
 */
@Path("/api/cart")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequestScoped
public class CartResource {

    @Inject
    Cart cart;

    /**
     * Initialize a new cart session.
     * 
     * POST /api/cart/initialize
     * 
     * Body: {"customerName": "John Doe", "customerId": "123"}
     */
    @POST
    @Path("/initialize")
    public Response initializeCart(CustomerRequest request) {
        try {
            if (request.customerId != null && !request.customerId.isEmpty()) {
                cart.initialize(request.customerName, request.customerId);
            } else {
                cart.initialize(request.customerName);
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "Cart initialized successfully");
            response.put("customerName", request.customerName);

            return Response.ok(response).build();
        } catch (BookException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    /**
     * Add a book to the cart.
     * 
     * POST /api/cart/books/{title}
     */
    @POST
    @Path("/books/{title}")
    public Response addBook(@PathParam("title") String title) {
        try {
            cart.addBook(title);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Book added successfully");
            response.put("title", title);
            response.put("cartSize", cart.getContents().size());

            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    /**
     * Remove a book from the cart.
     * 
     * DELETE /api/cart/books/{title}
     */
    @DELETE
    @Path("/books/{title}")
    public Response removeBook(@PathParam("title") String title) {
        try {
            cart.removeBook(title);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Book removed successfully");
            response.put("title", title);
            response.put("cartSize", cart.getContents().size());

            return Response.ok(response).build();
        } catch (BookException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    /**
     * Get all books in the cart.
     * 
     * GET /api/cart/books
     */
    @GET
    @Path("/books")
    public Response getBooks() {
        try {
            List<String> contents = cart.getContents();

            Map<String, Object> response = new HashMap<>();
            response.put("books", contents);
            response.put("count", contents.size());

            return Response.ok(response).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    /**
     * Clear the cart and end the session.
     * 
     * DELETE /api/cart
     */
    @DELETE
    public Response clearCart(@Context HttpServletRequest request) {
        try {
            // Simulate EJB @Remove by clearing the session and destroying the bean
            var session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }

            return Response.ok(Map.of("message", "Cart cleared successfully")).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", e.getMessage()))
                    .build();
        }
    }

    /**
     * Health check endpoint.
     * 
     * GET /api/cart/health
     */
    @GET
    @Path("/health")
    public Response health() {
        return Response.ok(Map.of("status", "UP", "service", "cart-api")).build();
    }

    // Request/Response DTOs
    public static class CustomerRequest {
        public String customerName;
        public String customerId;
    }
}
